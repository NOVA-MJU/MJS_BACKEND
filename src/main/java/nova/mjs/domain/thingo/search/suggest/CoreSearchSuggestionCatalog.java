package nova.mjs.domain.thingo.search.suggest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import nova.mjs.domain.thingo.semantic.SemanticTextNormalizer;
import nova.mjs.domain.thingo.semantic.TopicCatalog;
import nova.mjs.domain.thingo.semantic.TopicDefinition;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 공지 제목이 아니라 재사용 가능한 표준 핵심어만 제공하는 일반 검색 자동완성 Catalog.
 * 기존 검색 의도 사전과 공통 Topic Catalog를 읽으며 별도의 동의어 맵을 만들지 않는다.
 */
@Component
public class CoreSearchSuggestionCatalog {

    private static final String RESOURCE_PATH = "search/intent_lexicon.json";
    private static final int MAX_KEYWORD_LENGTH = 30;

    private final TopicCatalog topicCatalog;
    private final int minPrefixLength;
    private final List<IntentEntry> entries;

    public CoreSearchSuggestionCatalog(TopicCatalog topicCatalog) {
        this.topicCatalog = topicCatalog;
        LexiconFile file = load();
        this.minPrefixLength = file.meta() == null ? 2 : Math.max(2, file.meta().minCompletionPrefixLength());
        this.entries = file.entries() == null ? List.of() : List.copyOf(file.entries());
    }

    public List<String> suggest(String rawQuery, int limit) {
        String queryKey = SemanticTextNormalizer.lookupKey(rawQuery);
        if (queryKey.length() < minPrefixLength || limit <= 0) {
            return List.of();
        }

        Map<String, Candidate> bestByKey = new LinkedHashMap<>();

        for (IntentEntry entry : entries) {
            List<WeightedTerm> terms = weightedTerms(entry);
            boolean entryMatched = terms.stream()
                    .map(WeightedTerm::term)
                    .map(SemanticTextNormalizer::lookupKey)
                    .anyMatch(termKey -> isRelated(queryKey, termKey));
            if (!entryMatched) {
                continue;
            }
            terms.stream()
                    .filter(term -> isRelated(queryKey, SemanticTextNormalizer.lookupKey(term.term())))
                    .forEach(term -> addCandidate(bestByKey, term.term(), queryKey,
                            entry.weight() + term.sourceBoost(), true));
        }

        topicCatalog.autocomplete(rawQuery, 50, false).stream()
                .filter(TopicDefinition::searchable)
                .forEach(topic -> {
                    addCandidate(bestByKey, topic.displayName(), queryKey, 350, false);
                    if (topic.searchTerms() != null) {
                        topic.searchTerms().forEach(term ->
                                addCandidate(bestByKey, term, queryKey, 400, false));
                    }
                });

        return bestByKey.values().stream()
                .sorted(Comparator.comparingInt(Candidate::score).reversed()
                        .thenComparingInt(candidate -> candidate.value().length())
                        .thenComparing(Candidate::value))
                .limit(limit)
                .map(Candidate::value)
                .toList();
    }

    private List<WeightedTerm> weightedTerms(IntentEntry entry) {
        List<WeightedTerm> result = new ArrayList<>();
        result.add(new WeightedTerm(entry.intent(), 300));
        addAll(result, entry.matchKeywords(), 200);
        addAll(result, entry.expansions(), 100);
        return result;
    }

    private void addAll(List<WeightedTerm> target, List<String> values, int sourceBoost) {
        if (values != null) {
            values.forEach(value -> target.add(new WeightedTerm(value, sourceBoost)));
        }
    }

    private void addCandidate(Map<String, Candidate> target,
                              String value,
                              String queryKey,
                              int baseScore,
                              boolean requireDirectRelation) {
        String trimmed = value == null ? "" : value.trim();
        String valueKey = SemanticTextNormalizer.lookupKey(trimmed);
        if (trimmed.isBlank() || valueKey.length() < 2 || trimmed.length() > MAX_KEYWORD_LENGTH
                || (requireDirectRelation && !isRelated(queryKey, valueKey))) {
            return;
        }

        int matchScore;
        if (valueKey.equals(queryKey)) {
            matchScore = 10_000;
        } else if (valueKey.startsWith(queryKey)) {
            matchScore = 8_000;
        } else if (valueKey.contains(queryKey)) {
            matchScore = 6_000;
        } else {
            matchScore = 4_000;
        }

        Candidate candidate = new Candidate(trimmed, matchScore + baseScore);
        target.merge(valueKey, candidate,
                (left, right) -> left.score() >= right.score() ? left : right);
    }

    private boolean isRelated(String queryKey, String valueKey) {
        return !valueKey.isBlank()
                && (valueKey.contains(queryKey) || queryKey.contains(valueKey));
    }

    private LexiconFile load() {
        try (InputStream input = CoreSearchSuggestionCatalog.class.getClassLoader()
                .getResourceAsStream(RESOURCE_PATH)) {
            if (input == null) {
                throw new IllegalStateException("Search intent lexicon not found: " + RESOURCE_PATH);
            }
            return new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(input, LexiconFile.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load search intent lexicon", e);
        }
    }

    private record Candidate(String value, int score) {
    }

    private record WeightedTerm(String term, int sourceBoost) {
    }

    private record LexiconFile(LexiconMeta meta, List<IntentEntry> entries) {
    }

    private record LexiconMeta(int minCompletionPrefixLength) {
    }

    private record IntentEntry(
            String intent,
            int weight,
            List<String> matchKeywords,
            List<String> expansions
    ) {
    }
}
