package nova.mjs.domain.thingo.ElasticSearch.Service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.UnifiedSearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.SearchResponseDTO;
import nova.mjs.domain.thingo.ElasticSearch.SearchType;
import nova.mjs.domain.thingo.ElasticSearch.Repository.UnifiedSearchQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnifiedSearchService {

    private final UnifiedSearchQueryRepository unifiedSearchQueryRepository;

    /**
     * 통합 검색
     *
     * @param keyword  검색어
     * @param type     검색 타입 (nullable)
     * @param order    정렬 기준
     * @param pageable 페이징 정보
     */
    public Page<SearchResponseDTO> search(
            String keyword,
            String type,
            String order,
            Pageable pageable
    ) {
        SearchHits<UnifiedSearchDocument> hits =
                unifiedSearchQueryRepository.search(keyword, type, order, pageable);

        List<SearchResponseDTO> content = hits.getSearchHits()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    /**
     * 통합 검색 Overview
     *
     * - 도메인별 상위 N개 결과 반환
     * - SearchType.overviewOrder() 기준 순서 보장
     */
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

    /**
     * ES SearchHit → SearchResponseDTO 변환
     *
     * 정책
     * - ID: TYPE:ORIGINAL_ID
     * - type: 소문자 enum name
     * - highlight 없으면 원본 텍스트 fallback
     * - date는 Instant 그대로 유지
     */
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

    /**
     * 통합 문서 ID 생성
     *
     * 형식: TYPE:ORIGINAL_ID
     */
    private String buildUnifiedId(SearchType type, String originalId) {
        return type.name() + ":" + originalId;
    }

    /**
     * highlight 안전 추출
     *
     * - highlightFields 자체가 null 인 경우 방어
     * - 값이 없으면 fallback 반환
     */
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
}
