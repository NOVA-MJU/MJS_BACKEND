package nova.mjs.domain.thingo.ElasticSearch.search;

import java.util.List;
import java.util.Map;

/**
 * 검색 실행 계획 DTO.
 *
 * 역할:
 * - Resolver/Policy에서 결정한 신호를 Repository로 전달
 * - Repository는 이 값을 기반으로 ES DSL을 구성
 */
public record SearchQueryPlan(
        String keyword,
        String category,
        String order,
        List<String> expandedKeywords,
        Map<String, Integer> categoryBoosts,
        List<String> negativeKeywords,
        NegativeStrategy negativeStrategy,
        float negativeDownrankBoost,
        float expansionTermBoost,
        float autocompleteBoost,
        List<FreshnessRule> freshnessRules,
        List<PopularityRule> popularityRules
) {
    /** negative keyword 처리 정책. */
    public enum NegativeStrategy {
        HARD_FILTER,
        SOFT_DOWNRANK
    }

    /** 최신성 가중치 규칙. */
    public record FreshnessRule(String gte, float boost) {}

    /** 인기도 가중치 규칙. */
    public record PopularityRule(String field, int gte, float boost) {}
}
