package nova.mjs.util.ElasticSearch.Repository;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import lombok.RequiredArgsConstructor;
import nova.mjs.util.ElasticSearch.Document.CommunityDocument;
import nova.mjs.util.ElasticSearch.Document.NewsDocument;
import nova.mjs.util.ElasticSearch.Document.NoticeDocument;
import nova.mjs.util.ElasticSearch.Document.SearchDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public SearchHits<? extends SearchDocument> search(String keyword, String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<HighlightField> highlightFields = List.of(
                new HighlightField("title"),
                new HighlightField("content")
        );

        // 지정된 type에 맞는 SearchDocument 클래스를 결정
        Class<? extends SearchDocument> targetClass = resolveTargetClass(type);

        HighlightQuery highlightQuery = new HighlightQuery(
                new Highlight(highlightFields), targetClass
        );

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    BoolQuery.Builder boolBuilder = b
                            .should(s -> s.match(m -> m.field("title").query(keyword)))
                            .should(s -> s.match(m -> m.field("content").query(keyword)))
                            .minimumShouldMatch("1");

                    if (!type.isBlank()) {
                        boolBuilder.filter(f -> f.term(t -> t.field("type").value(type)));
                    }

                    return boolBuilder;
                }))
                .withHighlightQuery(highlightQuery)
                .withPageable(pageable)
                .build();

        // 검색 후, 결과를 반환
        return elasticsearchTemplate.search(query, targetClass);
    }

    private Class<? extends SearchDocument> resolveTargetClass(String type) {
        return switch (type.toLowerCase()) {
            case "notice" -> NoticeDocument.class;
            case "news" -> NewsDocument.class;
            case "community" -> CommunityDocument.class;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}