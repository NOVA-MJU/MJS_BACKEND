package nova.mjs.domain.thingo.ElasticSearch.suggest;

import java.util.List;
import java.util.Map;

/**
 * IntentLexiconEntry
 *
 * - 하나의 "사용자 의도(Intent)" 정의 단위
 * - matchKeywords로 문서/입력을 분류하고,
 * - expansions를 연관어로 제공하며,
 * - weight로 중요도를 부여한다.
 */
public record IntentLexiconEntry(
        String major,
        String minor,
        String intent,
        int weight,
        List<String> matchKeywords,
        List<String> expansions,
        List<String> negativeKeywords,
        Map<String, Integer> typeBoost
) {
}
