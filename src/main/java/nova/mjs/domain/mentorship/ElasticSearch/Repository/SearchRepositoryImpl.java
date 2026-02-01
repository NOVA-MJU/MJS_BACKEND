package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.ElasticSearch.Document.*;
import nova.mjs.domain.mentorship.ElasticSearch.SearchType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Repository;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;


import java.util.List;


@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public SearchHits<? extends SearchDocument> search(String keyword, SearchType type, String order, Pageable pageable) {

        HighlightParameters hlParams = HighlightParameters.builder()
                .withPreTags("<em>").withPostTags("</em>")
                .withType("unified").withForceSource(true)
                .withBoundaryScannerLocale("ko")
                .withRequireFieldMatch(false)   // 다른 필드 매칭도 추출 허용
                .build();

        List<HighlightField> highlightFields = List.of(
                new HighlightField("title"),
                new HighlightField("content"),
                new HighlightField("title.keepdot"),
                new HighlightField("content.keepdot")
        );

        // 지정된 type에 맞는 SearchDocument 클래스를 결정
        Class<? extends SearchDocument> targetClass = resolveTargetClass(type);

        HighlightQuery highlightQuery = new HighlightQuery(
                new Highlight(hlParams, highlightFields), targetClass
        );

        /**
         * Spring Data Elasticsearch DSL
         */
        long now   = System.currentTimeMillis();
        long d7    = now - java.time.Duration.ofDays(7).toMillis();
        long d30   = now - java.time.Duration.ofDays(30).toMillis();
        long d120  = now - java.time.Duration.ofDays(120).toMillis();
        long d360  = now - java.time.Duration.ofDays(360).toMillis();

        NativeQueryBuilder builder = NativeQuery.builder()
                .withQuery(q -> q
                        .functionScore(fs -> fs
                                .query(inner -> inner
                                        .bool(b -> b
                                                .should(s -> s.match(m -> m.field("title").query(keyword)))
                                                .should(s -> s.match(m -> m.field("content").query(keyword)))
                                                .should(s -> s.match(mt -> mt.field("title.keepdot").query(keyword)))
                                                .should(s -> s.match(mt -> mt.field("content.keepdot").query(keyword)))
                                        )
                                )
                                .functions(List.of(
                                        // 기존 정확도 가중치들
                                        FunctionScore.of(f -> f.filter(q1 -> q1.matchPhrase(mp -> mp.field("title").query(keyword))).weight(10.0)),
                                        FunctionScore.of(f -> f.filter(q2 -> q2.matchPhrase(mp -> mp.field("content").query(keyword))).weight(8.0)),
                                        FunctionScore.of(f -> f.filter(q3 -> q3.match(m -> m.field("title").query(keyword))).weight(2.0)),
                                        FunctionScore.of(f -> f.filter(q4 -> q4.match(m -> m.field("content").query(keyword))).weight(1.0)),

                                        // 최근 7일
                                        FunctionScore.of(f -> f.filter(q5 -> q5.range(r -> r.field("date").gte(JsonData.of(d7)))).weight(5.0)),

                                        // 7~30일:  gte now-30d AND lt now-7d
                                        FunctionScore.of(f -> f.filter(q6 -> q6.bool(b -> b
                                                .must(m -> m.range(r -> r.field("date").gte(JsonData.of(d30))))
                                                .must(m -> m.range(r -> r.field("date").lt(JsonData.of(d7))))
                                        )).weight(4.0)),

                                        // 30~120일: gte now-120d AND lt now-30d
                                        FunctionScore.of(f -> f.filter(q7 -> q7.bool(b -> b
                                                .must(m -> m.range(r -> r.field("date").gte(JsonData.of(d120))))
                                                .must(m -> m.range(r -> r.field("date").lt(JsonData.of(d30))))
                                        )).weight(2.3)),

                                        // 120~360일: gte now-360d AND lt now-120d
                                        FunctionScore.of(f -> f.filter(q8 -> q8.bool(b -> b
                                                .must(m -> m.range(r -> r.field("date").gte(JsonData.of(d360))))
                                                .must(m -> m.range(r -> r.field("date").lt(JsonData.of(d120))))
                                        )).weight(1.3))
                                ))
                                .scoreMode(FunctionScoreMode.Sum)   // 가중치들을 모두 더함 (title+content 다 걸리면 스코어 높아짐)
                                .boostMode(FunctionBoostMode.Sum)   // 기존 스코어와 가중치를 더해서 최종 점수 계산
                        )
                )
                .withHighlightQuery(highlightQuery)
                .withPageable(pageable)
                .withTrackTotalHits(true);

        // 정렬 분기
        String mode = (order == null) ? "relevance" : order.trim().toLowerCase();

        switch (mode) {
            case "latest":
                builder.withSort(s -> s.field(f -> f
                        .field("date")
                        .order(SortOrder.Desc)
                ));
                break;

            case "oldest":
                builder.withSort(s -> s.field(f -> f
                        .field("date")
                        .order(SortOrder.Asc)
                ));
                break;

            default: // relevance 기본: _score desc + date desc(타이브레이커)
                builder
                        .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                        .withSort(s -> s.field(f -> f
                                .field("date")
                        ));
        }


        NativeQuery query = builder.build();

        // 검색 후, 결과를 반환
        return elasticsearchTemplate.search(query, targetClass);
    }

    private Class<? extends SearchDocument> resolveTargetClass(SearchType type) {
        return switch (type) {
            case NOTICE -> NoticeDocument.class;
            case MJU_CALENDAR -> MjuCalendarDocument.class;
            case DEPARTMENT_NOTICE -> DepartmentNoticeDocument.class;
            case DEPARTMENT_SCHEDULE -> DepartmentScheduleDocument.class;
            case COMMUNITY -> CommunityDocument.class;
            case NEWS -> NewsDocument.class;
            case BROADCAST -> BroadcastDocument.class;
        };
    }
}