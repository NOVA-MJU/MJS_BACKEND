package nova.mjs.domain.thingo.ElasticSearch.search;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.suggest.IntentLexicon;
import nova.mjs.domain.thingo.ElasticSearch.suggest.IntentLexiconEntry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SearchIntentResolver {

    private static final int MAX_EXPANDED = 8;
    private static final int MAX_NEGATIVE = 6;

    private final IntentLexicon intentLexicon;

    public SearchIntentContext resolve(String keyword) {
        String normalized = normalize(keyword);
        if (normalized.isBlank()) {
            return SearchIntentContext.empty(normalized);
        }

        String compactKeyword = compact(normalized);
        String correctedKeyword = correctTypo(normalized);
        String correctedCompactKeyword = compact(correctedKeyword);

        List<IntentLexiconEntry> candidates = collectCandidates(
                normalized,
                compactKeyword,
                correctedKeyword,
                correctedCompactKeyword
        );

        Optional<IntentLexiconEntry> selected = selectBest(
                candidates,
                normalized,
                compactKeyword,
                correctedKeyword,
                correctedCompactKeyword
        );

        return selected
                .map(entry -> {
                    List<String> expandedKeywords = Stream.concat(
                                    Stream.of(entry.intent()),
                                    nullSafe(entry.expansions()).stream()
                            )
                            .flatMap(value -> Stream.of(value, compact(value)))
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(value -> !value.isBlank())
                            .distinct()
                            .limit(MAX_EXPANDED)
                            .toList();

                    List<String> negativeKeywords = nullSafe(entry.negativeKeywords()).stream()
                            .flatMap(value -> Stream.of(value, compact(value)))
                            .map(String::trim)
                            .filter(value -> !value.isBlank())
                            .distinct()
                            .limit(MAX_NEGATIVE)
                            .toList();

                    return new SearchIntentContext(
                            correctedKeyword,
                            expandedKeywords,
                            entry.typeBoost() == null ? Map.of() : entry.typeBoost(),
                            negativeKeywords
                    );
                })
                .orElse(SearchIntentContext.empty(correctedKeyword));
    }

    private List<IntentLexiconEntry> collectCandidates(
            String normalized,
            String compactKeyword,
            String correctedKeyword,
            String correctedCompactKeyword
    ) {
        List<IntentLexiconEntry> candidates = new ArrayList<>();

        intentLexicon.matchPrefix(normalized).ifPresent(candidates::add);
        if (!compactKeyword.equals(normalized)) {
            intentLexicon.matchPrefix(compactKeyword).ifPresent(candidates::add);
        }
        if (!correctedKeyword.equals(normalized)) {
            intentLexicon.matchPrefix(correctedKeyword).ifPresent(candidates::add);
        }
        if (!correctedCompactKeyword.equals(compactKeyword)) {
            intentLexicon.matchPrefix(correctedCompactKeyword).ifPresent(candidates::add);
        }

        if (candidates.isEmpty()) {
            candidates.addAll(intentLexicon.entries().stream()
                    .filter(entry ->
                            containsAny(entry, normalized)
                                    || containsAny(entry, compactKeyword)
                                    || containsAny(entry, correctedKeyword)
                                    || containsAny(entry, correctedCompactKeyword))
                    .toList());
        }

        return candidates.stream().distinct().toList();
    }

    private Optional<IntentLexiconEntry> selectBest(
            List<IntentLexiconEntry> candidates,
            String normalized,
            String compactKeyword,
            String correctedKeyword,
            String correctedCompactKeyword
    ) {
        return candidates.stream()
                .max(Comparator.comparingInt(entry ->
                        score(entry, normalized, compactKeyword, correctedKeyword, correctedCompactKeyword)));
    }

    private int score(
            IntentLexiconEntry entry,
            String normalized,
            String compactKeyword,
            String correctedKeyword,
            String correctedCompactKeyword
    ) {
        int score = entry.weight();

        for (String token : nullSafe(entry.matchKeywords())) {
            String normalizedToken = normalize(token);
            String compactToken = compact(normalizedToken);

            if (normalizedToken.equals(normalized) || normalizedToken.equals(correctedKeyword)) {
                score += 120;
            } else if (compactToken.equals(compactKeyword) || compactToken.equals(correctedCompactKeyword)) {
                score += 110;
            } else if (normalizedToken.startsWith(normalized) || normalizedToken.startsWith(correctedKeyword)) {
                score += 50;
            } else if (compactToken.startsWith(compactKeyword) || compactToken.startsWith(correctedCompactKeyword)) {
                score += 45;
            } else if (normalizedToken.contains(normalized) || normalizedToken.contains(correctedKeyword)) {
                score += 20;
            } else if (compactToken.contains(compactKeyword) || compactToken.contains(correctedCompactKeyword)) {
                score += 18;
            }
        }

        return score;
    }

    private boolean containsAny(IntentLexiconEntry entry, String query) {
        if (query == null || query.isBlank()) {
            return false;
        }

        return Stream.concat(
                        Stream.of(entry.intent()),
                        Stream.concat(nullSafe(entry.matchKeywords()).stream(), nullSafe(entry.expansions()).stream())
                )
                .filter(Objects::nonNull)
                .map(this::normalize)
                .anyMatch(value -> value.contains(query) || compact(value).contains(query) || query.contains(value));
    }

    private String correctTypo(String keyword) {
        String normalized = normalize(keyword);
        if (normalized.isBlank()) {
            return normalized;
        }

        String compactKeyword = compact(normalized);
        int allowedDistance = normalized.length() <= 4 ? 1 : 2;

        return intentLexicon.entries().stream()
                .flatMap(entry -> Stream.concat(
                        Stream.of(entry.intent()),
                        Stream.concat(nullSafe(entry.matchKeywords()).stream(), nullSafe(entry.expansions()).stream())
                ))
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .distinct()
                .map(candidate -> Map.entry(candidate, Math.min(
                        levenshtein(normalized, candidate),
                        levenshtein(compactKeyword, compact(candidate))
                )))
                .filter(entry -> entry.getValue() <= allowedDistance)
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(normalized);
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String compact(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").trim().toLowerCase(Locale.ROOT);
    }

    private List<String> nullSafe(List<String> values) {
        return values == null ? List.of() : values;
    }
}
