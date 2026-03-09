package nova.mjs.domain.thingo.ElasticSearch.Repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import nova.mjs.config.elasticsearch.KomoranTokenizerUtil;
import nova.mjs.domain.thingo.ElasticSearch.Document.UnifiedSearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.search.SearchQueryPlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 통합 검색 전용 Elasticsearch 쿼리 저장소.
 *
 * 설계 원칙:
 * - 정렬 기본값은 relevance이며, 같은 점수대에서는 최신 문서를 우선한다.
 * - 짧은 검색어와 긴 제목형 검색어를 같은 방식으로 처리하지 않는다.
 * - 본문 부분일치보다 제목/핵심 토큰 일치를 더 강한 신호로 본다.
 */
@Repository
@RequiredArgsConstructor
public class UnifiedSearchQueryRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 검색 계획을 Elasticsearch NativeQuery로 변환해 실행한다.
     *
     * 정렬 규칙:
     * - relevance: 점수 우선, 동점/근접점수는 최신 날짜 우선
     * - latest: 최신순
     * - oldest: 오래된순
     */
    public SearchHits<UnifiedSearchDocument> search(
            SearchQueryPlan plan,
            Pageable pageable
    ) {
        String normalizedKeyword = safe(plan.keyword());
        String compactKeyword = KomoranTokenizerUtil.compact(normalizedKeyword);
        List<String> queryTerms = KomoranTokenizerUtil.extractQueryTerms(normalizedKeyword);
        List<String> expandedKeywords = nullSafe(plan.expandedKeywords());

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query -> query.bool(boolQuery -> {

                    if (normalizedKeyword.isBlank()) {
                        boolQuery.must(m -> m.matchAll(ma -> ma));
                    } else {
                        // 검색어가 있을 때는 여러 신호를 하나의 bool 쿼리로 묶어 relevance를 계산한다.
                        boolQuery.must(m -> m.bool(keywordBool -> {
                            addPrimaryKeywordClauses(keywordBool, normalizedKeyword, plan);
                            addCompactKeywordClauses(keywordBool, compactKeyword, queryTerms, plan);
                            addStrictLongQueryClauses(keywordBool, compactKeyword, queryTerms, plan);
                            addExpansionClauses(keywordBool, expandedKeywords, plan.expansionTermBoost());
                            keywordBool.minimumShouldMatch("1");
                            return keywordBool;
                        }));
                    }

                    if (plan.type() != null) {
                        boolQuery.filter(filterQuery ->
                                filterQuery.term(termQuery ->
                                        termQuery.field("type").value(plan.type())
                                )
                        );
                    }

                    if (plan.category() != null) {
                        boolQuery.filter(filterQuery ->
                                filterQuery.term(termQuery ->
                                        termQuery.field("category").value(plan.category())
                                )
                        );
                    }

                    boolQuery.should(shouldQuery -> shouldQuery.term(termQuery ->
                            termQuery.field("type").value("NOTICE").boost(plan.noticeTypeBoost())
                    ));

                    boolQuery.should(shouldQuery -> shouldQuery.term(termQuery ->
                            termQuery.field("category").value("general").boost(plan.noticeGeneralCategoryBoost())
                    ));

                    plan.categoryBoosts().forEach((targetType, boost) -> {
                        if (targetType == null || boost == null || boost <= 0) {
                            return;
                        }

                        boolQuery.should(shouldQuery -> shouldQuery.term(termQuery ->
                                termQuery.field("type").value(targetType).boost(boost.floatValue())
                        ));
                    });

                    for (String negativeKeyword : nullSafe(plan.negativeKeywords())) {
                        if (plan.negativeStrategy() == SearchQueryPlan.NegativeStrategy.HARD_FILTER) {
                            boolQuery.mustNot(m -> m.multiMatch(mm -> mm
                                    .query(negativeKeyword)
                                    .fields("title", "category", "content", "searchTokens")
                            ));
                            continue;
                        }

                        // soft downrank는 완전 제외 대신 낮은 가중치의 should로 처리한다.
                        boolQuery.should(s -> s.multiMatch(mm -> mm
                                .query(negativeKeyword)
                                .fields("title", "category", "content", "searchTokens")
                                .boost(plan.negativeDownrankBoost())
                        ));
                    }

                    // relevance 정렬일 때만 최신성/인기도 가중치를 약하게 얹는다.
                    // 즉, 결과를 잘라내지는 않고 비슷한 문서들 사이에서만 최근 문서를 더 선호한다.
                    float recencyMultiplier = "relevance".equals(plan.order()) ? 0.55f : 1.0f;

                    for (SearchQueryPlan.FreshnessRule rule : nullSafe(plan.freshnessRules())) {
                        boolQuery.should(s -> s.range(r -> r
                                .field("date")
                                .gte(JsonData.of(rule.gte()))
                                .boost(rule.boost() * recencyMultiplier)
                        ));
                    }

                    for (SearchQueryPlan.PopularityRule rule : nullSafe(plan.popularityRules())) {
                        boolQuery.should(s -> s.range(r -> r
                                .field(rule.field())
                                .gte(JsonData.of(rule.gte()))
                                .boost(rule.boost() * recencyMultiplier)
                        ));
                    }

                    return boolQuery;
                }))
                .withSort(sortBuilder -> {
                    if ("latest".equals(plan.order())) {
                        return sortBuilder.field(fieldSort ->
                                fieldSort.field("date").order(SortOrder.Desc)
                        );
                    }

                    if ("oldest".equals(plan.order())) {
                        return sortBuilder.field(fieldSort ->
                                fieldSort.field("date").order(SortOrder.Asc)
                        );
                    }

                    return sortBuilder.score(scoreSort -> scoreSort);
                })
                .withSort(sortBuilder -> sortBuilder.field(fieldSort ->
                        fieldSort.field("date").order(SortOrder.Desc)
                ))
                .withPageable(pageable)
                .build();

        return elasticsearchOperations.search(nativeQuery, UnifiedSearchDocument.class);
    }

    /**
     * 사용자가 입력한 원문 검색어를 기준으로 가장 기본적인 매칭 신호를 추가한다.
     *
     * 포함하는 신호:
     * - 제목 exact phrase
     * - 제목/카테고리/본문/검색토큰 다중 매칭
     * - 자동완성 prefix 매칭
     */
    private void addPrimaryKeywordClauses(co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder keywordBool,
                                          String normalizedKeyword,
                                          SearchQueryPlan plan) {
        keywordBool.should(s -> s.matchPhrase(mp -> mp
                .field("title")
                .query(normalizedKeyword)
                .boost(plan.exactTitleMatchBoost())
        ));

        keywordBool.should(s -> s.multiMatch(mm -> mm
                .query(normalizedKeyword)
                .operator(Operator.Or)
                .fields(
                        "title^6",
                        "category^" + plan.categoryMatchBoost(),
                        "content^0.3",
                        "searchTokens^" + plan.searchTokenMatchBoost()
                )
        ));

        keywordBool.should(s -> s.matchPhrase(mp -> mp
                .field("searchTokens")
                .query(normalizedKeyword)
                .boost(plan.searchTokenMatchBoost() * 1.7f)
        ));

        keywordBool.should(s -> s.multiMatch(mm -> mm
                .query(normalizedKeyword)
                .type(TextQueryType.BoolPrefix)
                .fields(
                        "title_autocomplete^" + plan.autocompleteBoost(),
                        "title_autocomplete._2gram^3",
                        "title_autocomplete._3gram^2"
                )
        ));
    }

    /**
     * 공백 제거 검색어를 기준으로 복합명사 검색을 보강한다.
     *
     * 예:
     * - "해외 문화 탐방" -> "해외문화탐방"
     * - "전공학문연계 해외문화탐방" -> 공백 제거 후 titleNormalized/searchTokens에 매칭
     */
    private void addCompactKeywordClauses(co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder keywordBool,
                                          String compactKeyword,
                                          List<String> queryTerms,
                                          SearchQueryPlan plan) {
        if (compactKeyword.isBlank()) {
            return;
        }

        keywordBool.should(s -> s.matchPhrase(mp -> mp
                .field("titleNormalized")
                .query(compactKeyword)
                .boost(plan.compactTitleMatchBoost())
        ));

        keywordBool.should(s -> s.match(m -> m
                .field("searchTokens")
                .query(compactKeyword)
                .boost(plan.searchTokenMatchBoost() * 1.2f)
        ));

        keywordBool.should(s -> s.matchPhrase(mp -> mp
                .field("searchTokens")
                .query(compactKeyword)
                .boost(plan.compactTitleMatchBoost() * 1.8f)
        ));

        keywordBool.should(s -> s.match(m -> m
                .field("titleNormalized")
                .query(compactKeyword)
                .fuzziness("AUTO")
                .prefixLength(1)
                .boost(plan.typoFuzzyBoost())
        ));

        keywordBool.should(s -> s.match(m -> m
                .field("categoryNormalized")
                .query(compactKeyword)
                .fuzziness("AUTO")
                .prefixLength(1)
                .boost(plan.categoryMatchBoost())
        ));

        if (!queryTerms.isEmpty()) {
            keywordBool.should(s -> s.multiMatch(mm -> mm
                    .query(String.join(" ", queryTerms))
                    .fields("titleNormalized^8", "searchTokens^10")
                    .operator(Operator.And)
                    .boost(plan.compactTitleMatchBoost() * 2.2f)
            ));
        }
    }

    /**
     * 긴 제목형 검색어에 대해 더 엄격한 매칭을 추가한다.
     *
     * 의도:
     * - "2025학년도 동계 전공학문연계 해외문화탐방 참가자 선발안내"처럼
     *   이미 사용자가 제목을 거의 알고 있는 경우, 흔한 단어 일부만 맞는 문서가
     *   상단에 올라오지 못하게 한다.
     */
    private void addStrictLongQueryClauses(co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder keywordBool,
                                           String compactKeyword,
                                           List<String> queryTerms,
                                           SearchQueryPlan plan) {
        if (queryTerms.size() < 3) {
            return;
        }

        String normalizedTermQuery = String.join(" ", queryTerms.stream()
                .filter(term -> !term.equals(compactKeyword))
                .toList());

        if (!normalizedTermQuery.isBlank()) {
            keywordBool.should(s -> s.multiMatch(mm -> mm
                    .query(normalizedTermQuery)
                    .fields("title^9", "searchTokens^8")
                    .operator(Operator.And)
                    .boost(plan.exactTitleMatchBoost() * 2.4f)
            ));

            keywordBool.should(s -> s.multiMatch(mm -> mm
                    .query(normalizedTermQuery)
                    .fields("title^7", "searchTokens^7")
                    .minimumShouldMatch(resolveMinimumShouldMatch(queryTerms.size()))
                    .boost(plan.exactTitleMatchBoost() * 1.9f)
            ));
        }

        keywordBool.should(s -> s.matchPhrase(mp -> mp
                .field("titleNormalized")
                .query(compactKeyword)
                .boost(plan.compactTitleMatchBoost() * 2.6f)
        ));

        keywordBool.should(s -> s.matchPhrase(mp -> mp
                .field("searchTokens")
                .query(compactKeyword)
                .boost(plan.searchTokenMatchBoost() * 2.4f)
        ));
    }

    /**
     * intent 확장어를 relevance 계산에 반영한다.
     *
     * 단, 확장어는 recall을 넓히기 위한 보조 신호이며
     * 긴 제목형 검색에서 원문 exact 신호를 이기면 안 된다.
     */
    private void addExpansionClauses(co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder keywordBool,
                                     List<String> expandedKeywords,
                                     float expansionTermBoost) {
        for (String expandedKeyword : expandedKeywords) {
            String normalizedExpansion = safe(expandedKeyword);
            String compactExpansion = KomoranTokenizerUtil.compact(normalizedExpansion);

            keywordBool.should(s -> s.matchPhrase(mp -> mp
                    .field("title")
                    .query(normalizedExpansion)
                    .boost(expansionTermBoost * 1.5f)
            ));

            keywordBool.should(s -> s.multiMatch(mm -> mm
                    .query(normalizedExpansion)
                    .fields("title^4.5", "category^2.5", "content^0.2", "searchTokens^5.5")
                    .boost(expansionTermBoost)
            ));

            if (!compactExpansion.isBlank()) {
                keywordBool.should(s -> s.match(m -> m
                        .field("searchTokens")
                        .query(compactExpansion)
                        .boost(expansionTermBoost * 1.25f)
                ));

                keywordBool.should(s -> s.matchPhrase(mp -> mp
                        .field("searchTokens")
                        .query(compactExpansion)
                        .boost(expansionTermBoost * 1.9f)
                ));
            }
        }
    }

    /** null 입력을 빈 문자열로 바꾸고 trim 한다. */
    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /** null 리스트를 빈 리스트로 바꾼다. */
    private <T> List<T> nullSafe(List<T> values) {
        return values == null ? List.of() : values;
    }

    /**
     * 긴 검색어에서는 모든 토큰 완전 일치가 너무 엄격할 수 있으므로
     * 길이에 따라 minimum_should_match를 단계적으로 조정한다.
     */
    private String resolveMinimumShouldMatch(int termCount) {
        if (termCount >= 8) {
            return "70%";
        }
        if (termCount >= 5) {
            return "80%";
        }
        return Integer.toString(Math.max(2, termCount - 1));
    }
}
