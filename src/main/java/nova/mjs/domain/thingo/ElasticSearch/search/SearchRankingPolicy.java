package nova.mjs.domain.thingo.ElasticSearch.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 검색 랭킹 정책 조합기.
 *
 * 역할:
 * - IntentContext + 운영 정책값(Store)을 결합
 * - Repository가 소비할 SearchQueryPlan 생성
 */
@Component
@RequiredArgsConstructor
public class SearchRankingPolicy {

    private final SearchRankingPolicyStore policyStore;

    /**
     * 실행 시점 정책 스냅샷을 읽어 검색 계획을 구성한다.
     */
    public SearchQueryPlan plan(
            SearchIntentContext intentContext,
            String normalizedCategory,
            String order
    ) {
        return new SearchQueryPlan(
                intentContext.normalizedKeyword(),
                normalizedCategory,
                normalizeOrder(order),
                intentContext.expandedKeywords(),
                intentContext.categoryBoosts(),
                intentContext.negativeKeywords(),
                policyStore.snapshot().negativeStrategy(),
                policyStore.snapshot().negativeDownrankBoost(),
                policyStore.snapshot().expansionTermBoost(),
                policyStore.snapshot().autocompleteBoost(),
                policyStore.snapshot().noticeTypeBoost(),
                policyStore.snapshot().noticeGeneralCategoryBoost(),
                policyStore.snapshot().freshnessRules(),
                policyStore.snapshot().popularityRules()
        );
    }

    /** 정렬 파라미터 정규화(relevance 기본값). */
    private String normalizeOrder(String order) {
        if ("latest".equals(order) || "oldest".equals(order)) {
            return order;
        }
        return "relevance";
    }
}
