package nova.mjs.domain.thingo.ElasticSearch.search;

import java.util.List;
import java.util.Map;

/**
 * Query Understanding 결과 컨텍스트.
 *
 * 필드 역할:
 * - normalizedKeyword: 오탈자/정규화 적용된 최종 검색어
 * - expandedKeywords: Query Rewriting 확장 키워드
 * - categoryBoosts: 카테고리별 가중치
 * - negativeKeywords: 제외/감점 키워드
 */
public record SearchIntentContext(
        String normalizedKeyword,
        List<String> expandedKeywords,
        Map<String, Integer> categoryBoosts,
        List<String> negativeKeywords
) {
    public static SearchIntentContext empty(String normalizedKeyword) {
        return new SearchIntentContext(normalizedKeyword, List.of(), Map.of(), List.of());
    }
}
