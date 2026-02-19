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

/**
 * Query Understanding + Query Rewriting 담당 서비스.
 *
 * 주요 역할:
 * 1) 입력 정규화/오탈자 보정
 * 2) intent 후보 수집
 * 3) 스코어 기반 최종 intent 선택
 * 4) 확장/negative 키워드 생성
 */
@Service
@RequiredArgsConstructor
public class SearchIntentResolver {

    private static final int MAX_EXPANDED = 8;
    private static final int MAX_NEGATIVE = 6;

    private final IntentLexicon intentLexicon;

    /** 사용자 keyword를 검색 실행에 필요한 intent 컨텍스트로 변환한다. */
    public SearchIntentContext resolve(String keyword) {
        String normalized = normalize(keyword);
        if (normalized.isBlank()) {
            return SearchIntentContext.empty(normalized);
        }

        String compactKeyword = compact(normalized);
        String correctedKeyword = correctTypo(normalized);

        List<IntentLexiconEntry> candidates = collectCandidates(normalized, compactKeyword, correctedKeyword);
        Optional<IntentLexiconEntry> selected = selectBest(candidates, normalized, correctedKeyword);

        return selected
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
                            .limit(MAX_EXPANDED)
                            .toList();

                    List<String> negativeKeywords = nullSafe(entry.negativeKeywords()).stream()
                            .flatMap(v -> Stream.of(v, compact(v)))
                            .map(String::trim)
                            .filter(v -> !v.isBlank())
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

    /** raw/compact/corrected 기준으로 intent 후보를 수집한다. */
    private List<IntentLexiconEntry> collectCandidates(String normalized, String compactKeyword, String correctedKeyword) {
        List<IntentLexiconEntry> candidates = new ArrayList<>();

        intentLexicon.matchPrefix(normalized).ifPresent(candidates::add);
        if (!compactKeyword.equals(normalized)) {
            intentLexicon.matchPrefix(compactKeyword).ifPresent(candidates::add);
        }
        if (!correctedKeyword.equals(normalized)) {
            intentLexicon.matchPrefix(correctedKeyword).ifPresent(candidates::add);
        }

        if (candidates.isEmpty()) {
            candidates.addAll(intentLexicon.entries().stream()
                    .filter(entry -> containsAny(entry, normalized) || containsAny(entry, correctedKeyword))
                    .toList());
        }

        return candidates.stream().distinct().toList();
    }

    /** 후보 중 점수가 가장 높은 intent를 선택한다. */
    private Optional<IntentLexiconEntry> selectBest(
            List<IntentLexiconEntry> candidates,
            String normalized,
            String corrected
    ) {
        return candidates.stream()
                .max(Comparator.comparingInt(entry -> score(entry, normalized, corrected)));
    }

    /** intent 후보 점수 계산. */
    private int score(IntentLexiconEntry entry, String normalized, String corrected) {
        int score = entry.weight();

        for (String token : nullSafe(entry.matchKeywords())) {
            String lower = normalize(token);
            if (lower.equals(normalized) || lower.equals(corrected)) {
                score += 120;
            } else if (lower.startsWith(normalized) || lower.startsWith(corrected)) {
                score += 50;
            } else if (lower.contains(normalized) || lower.contains(corrected)) {
                score += 20;
            }
        }

        return score;
    }

    /** query와 entry vocabulary 간 포함 관계 여부. */
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
                .anyMatch(v -> v.contains(query) || query.contains(v));
    }

    /**
     * 오탈자 보정(Levenshtein 기반).
     * - 짧은 쿼리: 거리 1
     * - 긴 쿼리: 거리 2
     */
    private String correctTypo(String keyword) {
        String normalized = normalize(keyword);
        if (normalized.isBlank()) {
            return normalized;
        }

        int allowedDistance = normalized.length() <= 4 ? 1 : 2;

        return intentLexicon.entries().stream()
                .flatMap(entry -> Stream.concat(
                        Stream.of(entry.intent()),
                        Stream.concat(nullSafe(entry.matchKeywords()).stream(), nullSafe(entry.expansions()).stream())
                ))
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(v -> !v.isBlank())
                .distinct()
                .map(candidate -> Map.entry(candidate, levenshtein(normalized, candidate)))
                .filter(entry -> entry.getValue() <= allowedDistance)
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(normalized);
    }

    /** 편집거리 계산. */
    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

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

    /** 소문자/trim 정규화. */
    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /** 공백 제거 정규화. */
    private String compact(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").trim().toLowerCase(Locale.ROOT);
    }

    /** null-safe 리스트 처리. */
    private List<String> nullSafe(List<String> values) {
        return values == null ? List.of() : values;
    }
}
