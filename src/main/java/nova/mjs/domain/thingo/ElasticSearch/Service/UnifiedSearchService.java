package nova.mjs.domain.thingo.ElasticSearch.Service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.UnifiedSearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.Repository.UnifiedSearchQueryRepository;
import nova.mjs.domain.thingo.ElasticSearch.SearchResponseDTO;
import nova.mjs.domain.thingo.ElasticSearch.SearchType;
import nova.mjs.domain.thingo.ElasticSearch.search.SearchIntentContext;
import nova.mjs.domain.thingo.ElasticSearch.search.SearchIntentResolver;
import nova.mjs.domain.thingo.ElasticSearch.search.SearchQueryPlan;
import nova.mjs.domain.thingo.ElasticSearch.search.SearchRankingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnifiedSearchService {

    private final UnifiedSearchQueryRepository unifiedSearchQueryRepository;
    private final SearchIntentResolver searchIntentResolver;
    private final SearchRankingPolicy searchRankingPolicy;

    public Page<SearchResponseDTO> search(
            String keyword,
            String type,
            String order,
            Pageable pageable
    ) {
        String normalizedType = normalizeType(type);

        SearchIntentContext intentContext = searchIntentResolver.resolve(keyword);
        SearchQueryPlan plan = searchRankingPolicy.plan(intentContext, normalizedType, order);

        SearchHits<UnifiedSearchDocument> hits =
                unifiedSearchQueryRepository.search(plan, pageable);

        List<SearchResponseDTO> content = hits.getSearchHits()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    private SearchResponseDTO toResponse(SearchHit<UnifiedSearchDocument> hit) {
        UnifiedSearchDocument doc = hit.getContent();

        SearchType searchType = SearchType.from(doc.getType());

        String highlightedTitle = extractHighlight(
                hit,
                "title",
                doc.getTitle()
        );

        String highlightedContent = extractHighlight(
                hit,
                "content",
                doc.getContent()
        );

        return SearchResponseDTO.builder()
                .id(buildUnifiedId(searchType, doc.getOriginalId()))
                .highlightedTitle(highlightedTitle)
                .highlightedContent(highlightedContent)
                .date(doc.getDate())
                .link(doc.getLink())
                .category(doc.getCategory())
                .type(searchType.name().toLowerCase())
                .imageUrl(doc.getImageUrl())
                .score(hit.getScore())
                .authorName(doc.getAuthorName())
                .likeCount(doc.getLikeCount())
                .commentCount(doc.getCommentCount())
                .build();
    }

    private String buildUnifiedId(SearchType type, String originalId) {
        return type.name() + ":" + originalId;
    }

    private String extractHighlight(
            SearchHit<UnifiedSearchDocument> hit,
            String field,
            String fallback
    ) {
        if (hit.getHighlightFields() == null) {
            return fallback;
        }

        List<String> highlights = hit.getHighlightFields().get(field);

        if (highlights == null || highlights.isEmpty()) {
            return fallback;
        }

        return highlights.get(0);
    }

    private String normalizeType(String rawType) {
        SearchType parsedType = SearchType.from(rawType);
        return parsedType == null ? null : parsedType.name();
    }
}
