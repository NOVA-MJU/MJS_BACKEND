package nova.mjs.domain.thingo.ElasticSearch.Repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.UnifiedSearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.search.SearchQueryPlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 검색 쿼리 어댑터 Repository.
 *
 * 역할:
 * - SearchQueryPlan(정책 결과)을 Elasticsearch NativeQuery로 변환
 * - 실제 ES 검색 실행
 */
@Repository
@RequiredArgsConstructor
public class UnifiedSearchQueryRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 검색 계획(plan)을 기반으로 ES 쿼리를 구성해 실행한다.
     */
    public SearchHits<UnifiedSearchDocument> search(
            SearchQueryPlan plan,
            Pageable pageable
    ) {
        String normalizedKeyword = safe(plan.keyword());
        List<String> expandedKeywords = nullSafe(plan.expandedKeywords());

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query -> query.bool(boolQuery -> {

                    if (normalizedKeyword.isBlank()) {
                        boolQuery.must(m -> m.matchAll(ma -> ma));
                    } else {
                        boolQuery.must(m -> m.bool(keywordBool -> {
                            keywordBool.should(s -> s.multiMatch(mm -> mm
                                    .query(normalizedKeyword)
                                    .fields(
                                            "title^5",
                                            "title.ngram^4",
                                            "category^2",
                                            "content^1"
                                    )
                            ));

                            keywordBool.should(s -> s.multiMatch(mm -> mm
                                    .query(normalizedKeyword)
                                    .type(TextQueryType.BoolPrefix)
                                    .fields(
                                            "title_autocomplete^" + plan.autocompleteBoost(),
                                            "title_autocomplete._2gram",
                                            "title_autocomplete._3gram"
                                    )
                            ));

                            for (String expandedKeyword : expandedKeywords) {
                                keywordBool.should(s -> s.multiMatch(mm -> mm
                                        .query(expandedKeyword)
                                        .fields("title^4", "title.ngram^3", "category^2", "content")
                                        .boost(plan.expansionTermBoost())
                                ));

                                keywordBool.should(s -> s.matchPhrase(mp -> mp
                                        .field("title")
                                        .query(expandedKeyword)
                                        .boost(plan.expansionTermBoost() * 1.5f)
                                ));
                            }

                            String minimumShouldMatch = expandedKeywords.isEmpty() ? "1" : "2";
                            keywordBool.minimumShouldMatch(minimumShouldMatch);
                            return keywordBool;
                        }));
                    }



                    if (!expandedKeywords.isEmpty()) {
                        boolQuery.must(m -> m.bool(intentBool -> {
                            for (String expandedKeyword : expandedKeywords) {
                                intentBool.should(s -> s.matchPhrase(mp -> mp
                                        .field("title")
                                        .query(expandedKeyword)
                                        .boost(plan.expansionTermBoost() * 2.2f)
                                ));

                                intentBool.should(s -> s.multiMatch(mm -> mm
                                        .query(expandedKeyword)
                                        .fields("title^6", "title.ngram^4", "category^2", "content")
                                        .boost(plan.expansionTermBoost())
                                ));
                            }
                            intentBool.minimumShouldMatch("1");
                            return intentBool;
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

                    // 학교 대표 공지 우선 노출: NOTICE + category=general 강화
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
                                termQuery
                                        .field("type")
                                        .value(targetType)
                                        .boost(boost.floatValue())
                        ));
                    });

                    for (String negativeKeyword : nullSafe(plan.negativeKeywords())) {
                        if (plan.negativeStrategy() == SearchQueryPlan.NegativeStrategy.HARD_FILTER) {
                            boolQuery.mustNot(m -> m.multiMatch(mm -> mm
                                    .query(negativeKeyword)
                                    .fields("title", "title.ngram", "category", "content")
                            ));
                            continue;
                        }

                        boolQuery.should(s -> s.multiMatch(mm -> mm
                                .query(negativeKeyword)
                                .fields("title", "title.ngram", "category", "content")
                                .boost(plan.negativeDownrankBoost())
                        ));
                    }



                    if ("relevance".equals(plan.order()) && !expandedKeywords.isEmpty()) {
                        boolQuery.filter(filterQuery ->
                                filterQuery.range(rangeQuery -> rangeQuery
                                        .field("date")
                                        .gte(JsonData.of("now-" + plan.intentRecencyWindowDays() + "d/d"))
                                )
                        );
                    }

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

    /** null-safe 문자열 trim. */
    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /** null-safe 리스트 변환. */
    private <T> List<T> nullSafe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
