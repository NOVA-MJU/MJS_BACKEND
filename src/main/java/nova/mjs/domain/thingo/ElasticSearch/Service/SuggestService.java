package nova.mjs.domain.thingo.ElasticSearch.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.UnifiedSearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.suggest.IntentLexicon;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuggestService {

    private static final String SUGGESTION_NAME = "suggestion";
    private static final int DEFAULT_SIZE = 7;

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final IntentLexicon intentLexicon;

    /**
     * 자동완성 최종 정책
     *
     * 1) IntentLexicon 기반: 1글자에서도 의미 있는 연관어 제공
     * 2) Completion Suggest: prefix 기반 추천 (빠르고 정확)
     * 3) search_as_you_type: 부분 단어/중간 단어 보강 (특공대 같은 케이스)
     *
     * 결론:
     * - SuggestService만 바꾸는 게 아니라,
     *   인덱스에 suggest/title_autocomplete가 "정상 저장"되도록 Mapper/Document도 일치해야 한다.
     */
    public List<String> getSuggestions(String rawKeyword) {
        String keyword = normalize(rawKeyword);
        if (keyword.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> merged = new LinkedHashSet<>();

        // 1) 1글자에서도 동작: IntentLexicon 확장
        intentLexicon.matchPrefix(keyword).ifPresent(entry -> {
            add(merged, entry.intent());
            for (String ex : nullSafe(entry.expansions())) {
                add(merged, ex);
            }
        });

        // 2) completion: 1글자도 허용하되, 1글자일 때는 size를 줄여 노이즈를 완화
        int completionSize = (keyword.length() == 1) ? 5 : DEFAULT_SIZE;

        // 기존 meta 정책을 쓰되, "그냥 되도록"이 목표면 minPrefix를 1로 강제해도 됨
        int minPrefix = intentLexicon.meta().minCompletionPrefixLength();
        if (keyword.length() >= minPrefix) {
            merged.addAll(fetchCompletion(keyword, completionSize));
        }

        // 3) search_as_you_type은 2글자부터
        if (keyword.length() >= 2) {
            merged.addAll(fetchSearchAsYouType(keyword, DEFAULT_SIZE));
        }

        return merged.stream()
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .filter(v -> v.length() <= 50)
                .limit(DEFAULT_SIZE)
                .collect(Collectors.toList());
    }

    /**
     * Completion Suggest (prefix)
     */
    private List<String> fetchCompletion(String keyword, int size) {
        CompletionSuggester completion = CompletionSuggester.of(cb -> cb
                .field("suggest")
                .skipDuplicates(true)
                .size(size)
        );

        FieldSuggester fieldSuggester = FieldSuggester.of(fb -> fb
                .prefix(keyword)
                .completion(completion)
        );

        Suggester suggester = Suggester.of(s -> s
                .suggesters(Map.of(SUGGESTION_NAME, fieldSuggester))
        );

        NativeQuery query = NativeQuery.builder()
                .withSuggester(suggester)
                .build();

        SearchHits<UnifiedSearchDocument> hits =
                elasticsearchTemplate.search(query, UnifiedSearchDocument.class);

        Suggest suggest = hits.getSuggest();
        if (suggest == null) return List.of();

        Suggest.Suggestion<?> suggestion = suggest.getSuggestion(SUGGESTION_NAME);
        if (suggestion == null) return List.of();

        return suggestion.getEntries().stream()
                .flatMap(e -> e.getOptions().stream())
                .map(o -> o.getText())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .distinct()
                .toList();
    }

    /**
     * search_as_you_type 기반 보강
     *
     * - title_autocomplete / _2gram / _3gram에 bool_prefix로 조회
     * - 결과 문서 title을 suggestion 후보로 사용
     *
     * 주의:
     * - 이게 제대로 동작하려면 "title_autocomplete" 필드가 인덱스에 존재하고
     *   실제로 값이 들어가 있어야 한다. (Mapper에서 반드시 세팅 필요)
     */
    private List<String> fetchSearchAsYouType(String keyword, int size) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(mm -> mm
                        .query(keyword)
                        .type(TextQueryType.BoolPrefix)
                        .fields(
                                "title_autocomplete",
                                "title_autocomplete._2gram",
                                "title_autocomplete._3gram"
                        )
                ))
                .withMaxResults(size)
                .build();

        SearchHits<UnifiedSearchDocument> hits =
                elasticsearchTemplate.search(query, UnifiedSearchDocument.class);

        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(UnifiedSearchDocument::getTitle)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .distinct()
                .toList();
    }

    private void add(Set<String> set, String v) {
        if (v == null) return;
        String s = v.trim();
        if (s.isEmpty()) return;
        set.add(s);
    }

    private String normalize(String v) {
        return v == null ? "" : v.trim();
    }

    private List<String> nullSafe(List<String> v) {
        return v == null ? List.of() : v;
    }
}
