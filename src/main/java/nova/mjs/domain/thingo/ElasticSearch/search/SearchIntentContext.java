package nova.mjs.domain.thingo.ElasticSearch.search;

import java.util.List;
import java.util.Map;

/**
 * 검색어 의도 해석 결과.
 *
 * - IntentLexicon은 정책 저장소 역할만 수행하고,
 * - 실제 검색에 필요한 파생 정보는 이 컨텍스트로 전달한다.
 */
public record SearchIntentContext(
        String normalizedKeyword,
        List<String> expandedKeywords,
        Map<String, Integer> typeBoosts,
        List<String> negativeKeywords
) {
    public static SearchIntentContext empty(String normalizedKeyword) {
        return new SearchIntentContext(normalizedKeyword, List.of(), Map.of(), List.of());
    }
}
