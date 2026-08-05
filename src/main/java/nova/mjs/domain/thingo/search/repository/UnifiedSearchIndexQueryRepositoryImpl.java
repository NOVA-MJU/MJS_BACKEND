package nova.mjs.domain.thingo.search.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import nova.mjs.domain.thingo.search.dto.SearchResultRow;
import nova.mjs.domain.thingo.search.query.SearchQueryInterpreter;
import nova.mjs.domain.thingo.search.query.SearchQueryPlan;
import nova.mjs.domain.thingo.semantic.TopicCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PostgreSQL FTS + pg_trgm 기반 통합 검색 native 구현.
 *
 * 매칭 전략:
 *  - keyword 비어있음: 필터 + 정렬만 적용
 *  - keyword 있음: Komoran 토큰화 OR-tsquery 매칭 OR search_tokens % keyword (trigram)
 *    (둘 다 GIN 인덱스 사용 -> seq scan 없음)
 *
 * 점수:
 *  - ts_rank(search_vector, to_tsquery) * 0.6
 *  + 제목 매칭 시 0.25 부스트
 *  + similarity(search_tokens, keyword) * 0.2
 *  + coalesce(popularity, 0) * 0.0001
 */
@Repository
public class UnifiedSearchIndexQueryRepositoryImpl implements UnifiedSearchIndexQueryRepository {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private static final String ORDER_RELEVANCE = "relevance";
    private static final String ORDER_LATEST = "latest";
    private static final String ORDER_OLDEST = "oldest";

    private static final double SUGGEST_TRIGRAM_THRESHOLD = 0.2d;

    /*
     * 카테고리 가중치 (NOTICE 도메인 한정).
     * - 학생 체감 중요도: 일반(general)/학사(academic) 공지가 핵심. 장학(scholarship)·대외활동은
     *   소수만 찾는데도 최신성에 밀려 상단을 점령했다. recency 를 tie-breaker 로 약화한 만큼(아래)
     *   카테고리 격차를 키워 "일반/학사 > 장학/취업/활동"이 실제 순위에 드러나게 한다.
     * - rule(학칙)은 기한 없는 evergreen 문서라 academic 급(0.08)으로 우대(과거 0.00 → 묻힘 방지).
     * - 수강신청 공지 첨부 학사안내 원문(academic_source)은 사용자가 기준 파일을 즉시 열 수 있도록
     *   단일 기준 문서에만 0.80을 부여한다. 나머지 카테고리 boost는 0.11 이하로 유지한다.
     */
    private static final String CATEGORY_WEIGHT_EXPR =
            " CASE category "
                    + "  WHEN 'general' THEN 0.10 "
                    + "  WHEN 'academic_source' THEN 0.80 "
                    + "  WHEN 'academic_course_rule' THEN 0.11 "
                    + "  WHEN 'academic' THEN 0.09 "
                    + "  WHEN 'academic_rule' THEN 0.09 "
                    + "  WHEN 'graduation_2009_2014' THEN 0.09 "
                    + "  WHEN 'graduation_2015_2017' THEN 0.09 "
                    + "  WHEN 'graduation_2018_2024' THEN 0.09 "
                    + "  WHEN 'graduation_2025_plus' THEN 0.09 "
                    + "  WHEN 'rule' THEN 0.08 "
                    + "  WHEN 'career' THEN 0.03 "
                    + "  WHEN 'scholarship' THEN 0.02 "
                    + "  WHEN 'activity' THEN 0.02 "
                    + "  ELSE 0.00 "
                    + " END ";

    /*
     * 유효성(기한) 가중치.
     * - valid_until 이 과거면(마감 지남) 강한 감점(-0.50)으로 후순위.
     * - valid_until 이 미래면(아직 유효) 소폭 가점(+0.10)으로 우대.
     * - valid_until 이 null 이면(무기한, 학칙 등) 0 → 만료로 보지 않는다.
     * 관련도가 매우 높은 만료 문서는 완전히 사라지지 않고 후순위로만 내려간다(보수적 감점).
     * 주의: latest/oldest 정렬은 date 기준이라 이 가중치의 영향을 받지 않는다(정렬 의미 보존).
     */
    private static final String VALIDITY_WEIGHT_EXPR =
            " + CASE WHEN valid_until IS NOT NULL AND valid_until <  now() THEN -0.50 ELSE 0 END "
                    + " + CASE WHEN valid_until IS NOT NULL AND valid_until >= now() THEN  0.10 ELSE 0 END ";

    /*
     * 최신성(recency) 가중치 — tie-breaker 수준으로 약화(관련도 우선 정책).
     * - 과거엔 0.50(공고)이라 키워드 관련도(ts_rank*0.6, 보통 0.05~0.15)와 제목부스트(0.25/0.4)를
     *   압도해, 검색이 사실상 "키워드 1개라도 포함 + 최신순"으로 퇴화했다(관련 없는 최신글이 상단 점령).
     * - 이제 최신성은 "같은 관련도일 때만" 가르는 작은 가산점이다. 공고/학사일정 0.12, 그 외 0.08.
     *   제목 매칭(0.25/0.4)이나 카테고리 격차(0.08)를 뒤집지 못하는 크기로 둔다.
     * - 학사일정(MJU_CALENDAR) 최상단 노출은 recency 가 아니라 type 가중치(0.15)로 보장한다.
     * - 반감기 30일(2,592,000초). date 는 NOT NULL. 미래 발행은 GREATEST 로 0 클램프.
     * 주의: latest/oldest 정렬은 date 기준이라 이 가중치의 영향을 받지 않는다.
     */
    private static final String RECENCY_WEIGHT_EXPR =
            " + (CASE WHEN type IN ('NOTICE','DEPARTMENT_NOTICE','STUDENT_COUNCIL_NOTICE','MJU_CALENDAR') THEN 0.12 ELSE 0.08 END) "
                    + " * power(0.5, GREATEST(extract(epoch FROM (now() - date)), 0) / 2592000.0) ";

    /*
     * 도메인 타입 가중치.
     * - 학사일정(MJU_CALENDAR)을 최우선으로 노출한다(가장 높은 가중치 0.15). 학생이 가장 자주 찾는
     *   "언제 무슨 일정인지"가 같은 키워드면 공지보다 먼저 보여야 한다.
     * - 그 다음 학교 공지(NOTICE 0.10). 외부 콘텐츠(NEWS/BROADCAST)는 학교 공식 정보 대비
     *   우선순위가 낮아 음수(-0.05)로 명확히 하향한다.
     */
    private static final String TYPE_WEIGHT_EXPR =
            " CASE type "
                    + "  WHEN 'MJU_CALENDAR' THEN 0.15 "
                    + "  WHEN 'ACADEMIC_GUIDE' THEN 0.14 "
                    + "  WHEN 'NOTICE' THEN 0.10 "
                    + "  WHEN 'DEPARTMENT_SCHEDULE' THEN 0.06 "
                    + "  WHEN 'STUDENT_COUNCIL_NOTICE' THEN 0.05 "
                    + "  WHEN 'COMMUNITY' THEN 0.04 "
                    + "  WHEN 'NEWS' THEN -0.05 "
                    + "  WHEN 'BROADCAST' THEN -0.05 "
                    + "  ELSE 0.00 "
                    + " END ";

    @PersistenceContext
    private EntityManager em;

    private final SearchQueryInterpreter queryInterpreter;

    public UnifiedSearchIndexQueryRepositoryImpl() {
        // Spring Data JPA slice에서도 동일한 불변 Catalog를 사용해 저장소를 독립적으로 생성할 수 있게 한다.
        this.queryInterpreter = new SearchQueryInterpreter(new TopicCatalog());
    }

    @Override
    public Page<SearchResultRow> search(String keyword,
                                        String type,
                                        String category,
                                        String order,
                                        String hotPattern,
                                        double hotBoost,
                                        Pageable pageable) {

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasHot = hotPattern != null && !hotPattern.isBlank() && hotBoost > 0d;
        boolean isNoticeSearchGroup = type != null && "NOTICE".equalsIgnoreCase(type.trim());
        String resolvedOrder = resolveOrder(order, hasKeyword);

        // 원문을 바로 OR 검색하지 않고 후보(match)·랭킹(rank)·커버리지(coverage) 계획으로 분리한다.
        // 등록된 약어/특수 표현은 표준 개념이 후보의 필수 조건이 되고, 일반 검색은 기존 Komoran OR 전략을 유지한다.
        SearchQueryPlan queryPlan = hasKeyword
                ? queryInterpreter.interpret(keyword)
                : new SearchQueryPlan("", "", "", "", List.of());
        String matchTsQuery = queryPlan.matchTsQuery();
        String rankTsQuery = queryPlan.rankTsQuery();
        String coverageTsQuery = queryPlan.coverageTsQuery();
        boolean hasMatchTsQuery = !matchTsQuery.isBlank();
        boolean hasRankTsQuery = !rankTsQuery.isBlank();
        boolean hasCoverageTsQuery = !coverageTsQuery.isBlank();
        List<String> topicIds = queryPlan.topicIds() == null ? List.of() : queryPlan.topicIds();
        boolean hasTopicIds = !topicIds.isEmpty();
        boolean isBroadGlobalProgramQuery = hasKeyword
                && topicIds.size() == 1
                && "GLOBAL_PROGRAM".equals(topicIds.get(0))
                && keyword.contains("해외");

        // 키워드가 있으나 의미 토큰이 전혀 없으면(자모/기호 노이즈) 매칭 대상이 없다.
        // DB 조회 없이 빈 결과를 즉시 반환한다(노이즈 입력이 느린 trigram 스캔을 타지 않도록).
        if (hasKeyword && !hasMatchTsQuery && !hasTopicIds) {
            return new PageImpl<>(java.util.List.of(), pageable, 0L);
        }

        StringBuilder where = new StringBuilder(" WHERE active = TRUE ");
        if (isNoticeSearchGroup) {
            // 사용자 화면의 '공지사항'은 원문 공지와 그 첨부물을 구조화한 학사안내문을
            // 하나의 검색 그룹으로 보여준다. 실제 응답 type은 그대로 보존한다.
            where.append(" AND type IN ('NOTICE', 'ACADEMIC_GUIDE') ");
        } else if (type != null && !type.isBlank()) {
            where.append(" AND type = :type ");
        }
        if (category != null && !category.isBlank()) {
            where.append(" AND category = :category ");
        }
        if (hasKeyword) {
            // 핵심 개념이 인식되면 해당 개념 그룹이 필수 후보 조건이 된다.
            List<String> candidateConditions = new ArrayList<>();
            if (hasMatchTsQuery && !isBroadGlobalProgramQuery) {
                candidateConditions.add("search_vector @@ to_tsquery('simple', :matchTsQuery)");
            }
            for (int i = 0; i < topicIds.size(); i++) {
                candidateConditions.add("topic_ids @> CAST(:topicId" + i + " AS jsonb)");
            }
            where.append(" AND (").append(String.join(" OR ", candidateConditions)).append(") ");
        }

        String topicMatchExpr = buildTopicMatchExpression(topicIds);
        String topicBoostExpr = hasTopicIds
                ? " + CASE WHEN " + topicMatchExpr + " THEN 0.65 ELSE 0 END "
                : " ";

        String hotBoostExpr = hasHot
                ? " + CASE WHEN coalesce(title,'') ~* :hotPattern THEN :hotBoost ELSE 0 END "
                : " ";

        // 카테고리/타입 가중치는 keyword 유무와 무관하게 항상 합산한다.
        // - keyword 있을 때: ts_rank/제목부스트가 dominant, 가중치는 tie-breaker 수준.
        // - keyword 없을 때: 가중치 + popularity 가 정렬 기준.
        String weightExpr = " + " + CATEGORY_WEIGHT_EXPR + " + " + TYPE_WEIGHT_EXPR + " ";

        // FTS 점수: ts_rank + 제목 매칭 부스트(본문에만 스친 문서가 제목 정매칭을 누르지 않도록).
        // 제목 부스트는 WHERE 통과 행에만 계산되므로(인덱스 필터 후) 성능 부담 없음.
        // 점수는 ts_rank + 제목 매칭 부스트만 사용한다.
        // trigram similarity(search_tokens, keyword) 는 KB 급 토큰 blob 을 매칭된 전 행에 대해
        // 재계산하므로 지연의 주원인이었다. trigram 은 WHERE(% 연산자, GIN 인덱스)에서 매칭에만 쓰고
        // 랭킹에서는 제거한다.
        // 제목이 모든 토큰을 포함하면 강한 가산점(본문에만 co-occur 하는 문서보다 우선).
        // 제목 매칭은 미리 저장된 title_vector 를 사용한다(행마다 to_tsvector(title) 재파싱 제거).
        String coverageBoost = hasCoverageTsQuery
                ? " + CASE WHEN title_vector @@ to_tsquery('simple', :coverageTsQuery) "
                + "        THEN 0.4 ELSE 0 END "
                : " ";
        String ftsScore = hasRankTsQuery
                ? " ts_rank(search_vector, to_tsquery('simple', :rankTsQuery)) * 0.6 "
                + " + CASE WHEN title_vector @@ to_tsquery('simple', :matchTsQuery) "
                + "        THEN 0.25 ELSE 0 END "
                + coverageBoost
                : " 0.0 ";

        String scoreExpr = hasKeyword
                ? " ( " + ftsScore
                + "  + coalesce(popularity, 0) * 0.0001 "
                + weightExpr
                + topicBoostExpr
                + hotBoostExpr
                + VALIDITY_WEIGHT_EXPR
                + RECENCY_WEIGHT_EXPR
                + " ) "
                : (hasHot
                ? " (coalesce(popularity, 0) * 0.0001 " + weightExpr + hotBoostExpr + VALIDITY_WEIGHT_EXPR + RECENCY_WEIGHT_EXPR + ") "
                : " (0.0 " + weightExpr + VALIDITY_WEIGHT_EXPR + RECENCY_WEIGHT_EXPR + ") ");

        // 제목은 항상 전체를 반환한다(HighlightAll=TRUE). 키워드가 제목에 없으면(본문/토큰 매칭)
        // MaxFragments 방식은 제목을 짧은 앞부분 조각으로 잘라버린다("[교외근로" 처럼).
        // 제목은 짧으므로 전체를 보여주고 매칭 구간만 <em> 표시한다.
        String headlineTitle = hasRankTsQuery
                ? " ts_headline('simple', coalesce(title,''), to_tsquery('simple', :rankTsQuery), "
                + " 'StartSel=<em>,StopSel=</em>,HighlightAll=TRUE') "
                : " title ";

        // 본문 스니펫: 매칭 주변 조각 2개를 ' ... ' 로 이어 풍부한 맥락을 보여준다.
        // - MaxFragments=2: 매칭 단어 주위 창을 최대 2개(왜 검색에 걸렸는지 보이게).
        // - MinWords=12: 너무 짧은 조각(한두 단어) 방지 → 문장처럼 읽힘.
        // - MaxWords=28: 조각당 상한(카드에서 과도하게 길어지지 않게, 최종 줄수는 프론트가 clamp).
        // - 키워드가 본문에 없으면(제목/토큰 매칭) 앞부분을 리드 문구로 반환.
        String headlineContent = hasRankTsQuery
                ? " ts_headline('simple', coalesce(content,''), to_tsquery('simple', :rankTsQuery), "
                + " 'StartSel=<em>,StopSel=</em>,MaxFragments=2,MinWords=12,MaxWords=28,FragmentDelimiter= ... ') "
                : " content ";

        String orderBy = switch (resolvedOrder) {
            case ORDER_LATEST -> " ORDER BY date DESC NULLS LAST ";
            case ORDER_OLDEST -> " ORDER BY date ASC NULLS LAST ";
            default -> " ORDER BY score DESC, date DESC NULLS LAST ";
        };

        // 현재 공지사항 UI는 응답 type=NOTICE만 렌더링한다. 검색 인덱스의 원본 타입은
        // ACADEMIC_GUIDE로 유지하되, NOTICE 검색 그룹의 응답만 호환 타입으로 변환한다.
        String responseTypeExpr = isNoticeSearchGroup
                ? " CASE WHEN type = 'ACADEMIC_GUIDE' THEN 'NOTICE' ELSE type END "
                : " type ";

        String selectSql =
                "SELECT id, original_id, " + responseTypeExpr + " AS type, category, title, "
                        + headlineTitle + " AS highlighted_title, "
                        + " content, "
                        + headlineContent + " AS highlighted_content, "
                        + " author_name, link, image_url, like_count, comment_count, date, "
                        + " CAST(topic_ids AS text), CAST(direct_topic_ids AS text), "
                        + scoreExpr + " AS score "
                        + " FROM unified_search_index "
                        + where
                        + orderBy
                        + " LIMIT :limit OFFSET :offset ";

        String countSql = "SELECT count(*) FROM unified_search_index " + where;

        Query selectQuery = em.createNativeQuery(selectSql);
        Query countQuery = em.createNativeQuery(countSql);

        String boundType = isNoticeSearchGroup ? null : type;
        bindMatchParams(selectQuery, matchTsQuery, hasMatchTsQuery, boundType, category, topicIds);
        bindMatchParams(
                countQuery,
                matchTsQuery,
                hasMatchTsQuery && !isBroadGlobalProgramQuery,
                boundType,
                category,
                topicIds
        );

        // 랭킹/커버리지 쿼리는 SELECT 점수식과 headline 에만 존재한다.
        if (hasRankTsQuery) {
            selectQuery.setParameter("rankTsQuery", rankTsQuery);
        }
        if (hasCoverageTsQuery) {
            selectQuery.setParameter("coverageTsQuery", coverageTsQuery);
        }

        if (hasHot) {
            selectQuery.setParameter("hotPattern", hotPattern);
            selectQuery.setParameter("hotBoost", hotBoost);
        }

        selectQuery.setParameter("limit", pageable.getPageSize());
        selectQuery.setParameter("offset", pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = selectQuery.getResultList();
        long total = ((Number) countQuery.getSingleResult()).longValue();

        List<SearchResultRow> content = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            content.add(toRow(r));
        }

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<String> suggest(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        String sql =
                "SELECT title FROM unified_search_index "
                        + " WHERE active = TRUE "
                        + "   AND ( title ILIKE :prefix "
                        + "         OR word_similarity(:keyword, title) > :trgmThreshold ) "
                        + " ORDER BY word_similarity(:keyword, title) DESC, date DESC NULLS LAST "
                        + " LIMIT :limit ";

        Query q = em.createNativeQuery(sql);
        q.setParameter("prefix", keyword + "%");
        q.setParameter("keyword", keyword);
        q.setParameter("trgmThreshold", SUGGEST_TRIGRAM_THRESHOLD);
        q.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Object> result = q.getResultList();
        return result.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .distinct()
                .toList();
    }

    private void bindMatchParams(Query q,
                                 String matchTsQuery,
                                 boolean hasMatchTsQuery,
                                 String type,
                                 String category,
                                 List<String> topicIds) {
        if (type != null && !type.isBlank()) {
            q.setParameter("type", type);
        }
        if (category != null && !category.isBlank()) {
            q.setParameter("category", category);
        }
        if (hasMatchTsQuery) {
            q.setParameter("matchTsQuery", matchTsQuery);
        }
        for (int i = 0; i < topicIds.size(); i++) {
            q.setParameter("topicId" + i, "[\"" + topicIds.get(i) + "\"]");
        }
    }

    private String buildTopicMatchExpression(List<String> topicIds) {
        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < topicIds.size(); i++) {
            conditions.add("topic_ids @> CAST(:topicId" + i + " AS jsonb)");
        }
        return conditions.isEmpty() ? "FALSE" : "(" + String.join(" OR ", conditions) + ")";
    }

    private String resolveOrder(String order, boolean hasKeyword) {
        if (order == null || order.isBlank()) {
            return hasKeyword ? ORDER_RELEVANCE : ORDER_LATEST;
        }
        String lower = order.toLowerCase();
        if (ORDER_LATEST.equals(lower) || ORDER_OLDEST.equals(lower) || ORDER_RELEVANCE.equals(lower)) {
            return lower;
        }
        return hasKeyword ? ORDER_RELEVANCE : ORDER_LATEST;
    }

    private SearchResultRow toRow(Object[] r) {
        return new SearchResultRow(
                asString(r[0]),
                asString(r[1]),
                asString(r[2]),
                asString(r[3]),
                asString(r[4]),
                asString(r[5]),
                asString(r[6]),
                asString(r[7]),
                asString(r[8]),
                asString(r[9]),
                asString(r[10]),
                asInt(r[11]),
                asInt(r[12]),
                asInstant(r[13]),
                asStringList(r[14]),
                asStringList(r[15]),
                asDouble(r[16])
        );
    }

    private List<String> asStringList(Object value) {
        if (value == null) return List.of();
        try {
            return OBJECT_MAPPER.readValue(value.toString(), STRING_LIST_TYPE);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String asString(Object v) {
        return v == null ? null : v.toString();
    }

    private Integer asInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        return Integer.valueOf(v.toString());
    }

    private Double asDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof BigDecimal b) return b.doubleValue();
        return Double.valueOf(v.toString());
    }

    private Instant asInstant(Object v) {
        if (v == null) return null;
        if (v instanceof Instant i) return i;
        if (v instanceof Timestamp t) return t.toInstant();
        if (v instanceof java.sql.Date d) return d.toInstant();
        if (v instanceof java.time.OffsetDateTime odt) return odt.toInstant();
        return null;
    }
}
