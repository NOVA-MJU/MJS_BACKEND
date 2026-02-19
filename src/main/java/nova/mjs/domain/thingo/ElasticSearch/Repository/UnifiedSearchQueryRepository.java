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
 * UnifiedSearchQueryRepository
 *
 * 정책 객체(SearchQueryPlan)를 Elasticsearch query로 변환하는 어댑터 역할만 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class UnifiedSearchQueryRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchHits<UnifiedSearchDocument> search(
            SearchQueryPlan plan,
            Pageable pageable
    ) {
        String normalizedKeyword = safe(plan.keyword());

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

                            for (String expandedKeyword : nullSafe(plan.expandedKeywords())) {
                                keywordBool.should(s -> s.multiMatch(mm -> mm
                                        .query(expandedKeyword)
                                        .fields("title^3", "title.ngram^2", "category^2", "content")
                                        .boost(plan.expansionTermBoost())
                                ));
                            }

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

                    plan.typeBoosts().forEach((targetType, boost) -> {
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

                    for (SearchQueryPlan.FreshnessRule rule : nullSafe(plan.freshnessRules())) {
                        boolQuery.should(s -> s.range(r -> r.date(d -> d
                                .field("date")
                                .gte(JsonData.of(rule.gte()))
                                .boost(rule.boost())
                        )));
                    }

                    for (SearchQueryPlan.PopularityRule rule : nullSafe(plan.popularityRules())) {
                        boolQuery.should(s -> s.range(r -> r.number(n -> n
                                .field(rule.field())
                                .gte(JsonData.of(rule.gte()))
                                .boost(rule.boost())
                        )));
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

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private <T> List<T> nullSafe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
