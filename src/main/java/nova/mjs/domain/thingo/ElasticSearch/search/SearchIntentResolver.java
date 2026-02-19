package nova.mjs.domain.thingo.ElasticSearch.search;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.suggest.IntentLexicon;
import nova.mjs.domain.thingo.ElasticSearch.suggest.IntentLexiconEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * SearchIntentResolver
 *
 * 입력 keyword를 의도 중심 신호로 해석한다.
 * - intent/expansion/typeBoost/negativeKeywords 추출
 * - 띄어쓰기 차이(예: 졸업요건 vs 졸업 요건)도 보완한다.
 */
@Service
@RequiredArgsConstructor
public class SearchIntentResolver {

    private final IntentLexicon intentLexicon;

    public SearchIntentContext resolve(String keyword) {
        String normalized = normalize(keyword);
        if (normalized.isBlank()) {
            return SearchIntentContext.empty(normalized);
        }

        String compactKeyword = compact(normalized);

        Optional<IntentLexiconEntry> matched = intentLexicon.matchPrefix(normalized);
        if (matched.isEmpty() && !compactKeyword.equals(normalized)) {
            matched = intentLexicon.matchPrefix(compactKeyword);
        }

        return matched
                .map(entry -> {
                    List<String> expandedKeywords = Stream.concat(
                                    Stream.of(entry.intent()),
                                    nullSafe(entry.expansions()).stream()
                            )
                            .flatMap(v -> Stream.of(v, compact(v)))
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(v -> !v.isBlank())
                            .distinct()
                            .limit(8)
                            .toList();

                    List<String> negativeKeywords = nullSafe(entry.negativeKeywords()).stream()
                            .flatMap(v -> Stream.of(v, compact(v)))
                            .map(String::trim)
                            .filter(v -> !v.isBlank())
                            .distinct()
                            .limit(6)
                            .toList();

                    return new SearchIntentContext(
                            normalized,
                            expandedKeywords,
                            entry.typeBoost() == null ? java.util.Map.of() : entry.typeBoost(),
                            negativeKeywords
                    );
                })
                .orElse(SearchIntentContext.empty(normalized));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String compact(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").trim();
    }

    private List<String> nullSafe(List<String> values) {
        return values == null ? List.of() : values;
    }
}
