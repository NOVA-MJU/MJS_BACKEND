package nova.mjs.domain.thingo.ElasticSearch.Service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.UnifiedSearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.Repository.UnifiedSearchQueryRepository;
import nova.mjs.domain.thingo.ElasticSearch.SearchResponseDTO;
import nova.mjs.domain.thingo.ElasticSearch.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnifiedSearchService {

    private final UnifiedSearchQueryRepository unifiedSearchQueryRepository;

    public Page<SearchResponseDTO> search(
            String keyword,
            String type,
            String order,
            Pageable pageable
    ) {
        SearchHits<UnifiedSearchDocument> hits =
                unifiedSearchQueryRepository.search(keyword, type, order, pageable);

        List<SearchResponseDTO> content = hits.getSearchHits().stream()
                .map(this::toDTO)
                .toList();

        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    public Map<String, List<SearchResponseDTO>> overview(
            String keyword,
            String order,
            int pageSize
    ) {
        Map<String, List<SearchResponseDTO>> result = new LinkedHashMap<>();
        Pageable pageable = PageRequest.of(0, pageSize);

        for (SearchType type : SearchType.overviewOrder()) {
            result.put(
                    type.name().toLowerCase(),
                    search(keyword, type.name(), order, pageable).getContent()
            );
        }
        return result;
    }

    private SearchResponseDTO toDTO(SearchHit<UnifiedSearchDocument> hit) {

        UnifiedSearchDocument doc = hit.getContent();

        LocalDateTime dateTime =
                doc.getDate() != null
                        ? LocalDateTime.ofInstant(
                        doc.getDate(),
                        ZoneId.systemDefault()
                )
                        : null;

        return new SearchResponseDTO(
                doc.getOriginalId(),
                doc.getTitle(),
                doc.getContent(),
                dateTime,
                doc.getLink(),
                doc.getCategory(),
                doc.getType(),
                doc.getImageUrl(),
                hit.getScore()
        );
    }
}
