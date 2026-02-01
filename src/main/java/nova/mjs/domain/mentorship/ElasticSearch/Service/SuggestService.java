package nova.mjs.domain.mentorship.ElasticSearch.Service;

import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.ElasticSearch.Document.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest.Suggestion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest.Suggestion.Entry;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest.Suggestion.Entry.Option;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SuggestService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    public List<String> getSuggestions(String keyword) {
        CompletionSuggester completion = CompletionSuggester.of(cb -> cb
                .field("suggest")
                .skipDuplicates(true)
                .size(10)
        );

        FieldSuggester fieldSuggester = FieldSuggester.of(fb -> fb
                .prefix(keyword)
                .completion(completion)
        );

        Suggester suggester = Suggester.of(s -> s
                .suggesters(Map.of("suggestion", fieldSuggester))
        );

        NativeQuery query = NativeQuery.builder()
                .withSuggester(suggester)
                .build();

        List<String> results = Stream.of(
                        NoticeDocument.class,
                        DepartmentScheduleDocument.class,
                        DepartmentNoticeDocument.class,
                        CommunityDocument.class,
                        NewsDocument.class,
                        BroadcastDocument.class,
                        MjuCalendarDocument.class
                ).map(docClass -> elasticsearchTemplate.search(query, docClass)) // SearchHits<T>
                .map(SearchHits::getSuggest)
                .filter(Objects::nonNull)
                .map(s -> (Suggestion<Entry<Option>>) s.getSuggestion("suggestion"))
                .filter(Objects::nonNull)
                .flatMap(s -> s.getEntries().stream())
                .flatMap(entry -> entry.getOptions().stream())
                .map(Option::getText)
                .distinct()
                .toList();

        return results;
    }
}