package nova.mjs.domain.thingo.ElasticSearch.search;

import java.util.List;
import java.util.Map;

/**
 * Repository에서 Elasticsearch query로 변환할 최종 계획 객체.
 */
public record SearchQueryPlan(
        String keyword,
        String type,
        String order,
        List<String> expandedKeywords,
        Map<String, Integer> typeBoosts,
        List<String> negativeKeywords,
        NegativeStrategy negativeStrategy,
        float negativeDownrankBoost,
        float expansionTermBoost,
        float autocompleteBoost,
        List<FreshnessRule> freshnessRules,
        List<PopularityRule> popularityRules
) {
    public enum NegativeStrategy {
        HARD_FILTER,
        SOFT_DOWNRANK
    }

    public record FreshnessRule(String gte, float boost) {}

    public record PopularityRule(String field, int gte, float boost) {}
}
