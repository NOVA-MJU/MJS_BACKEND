package nova.mjs.domain.thingo.ElasticSearch.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SearchRankingPolicy
 *
 * BM25 기본 점수 위에 의도/최신성/인기도 신호를 어떻게 얹을지 정책을 관리한다.
 */
@Component
@RequiredArgsConstructor
public class SearchRankingPolicy {

    private final SearchRankingPolicyStore policyStore;

    public SearchQueryPlan plan(
            SearchIntentContext intentContext,
            String normalizedType,
            String order
    ) {
        return new SearchQueryPlan(
                intentContext.normalizedKeyword(),
                normalizedType,
                normalizeOrder(order),
                intentContext.expandedKeywords(),
                intentContext.typeBoosts(),
                intentContext.negativeKeywords(),
                policyStore.snapshot().negativeStrategy(),
                policyStore.snapshot().negativeDownrankBoost(),
                policyStore.snapshot().expansionTermBoost(),
                policyStore.snapshot().autocompleteBoost(),
                policyStore.snapshot().freshnessRules(),
                policyStore.snapshot().popularityRules()
        );
    }

    private String normalizeOrder(String order) {
        if ("latest".equals(order) || "oldest".equals(order)) {
            return order;
        }
        return "relevance";
    }
}
