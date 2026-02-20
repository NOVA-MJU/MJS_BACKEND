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

/**
 * 통합 검색 오케스트레이션 서비스.
 *
 * 역할:
 * 1) Query Understanding/Rewrite 결과 생성
 * 2) Ranking policy 적용 계획 생성
 * 3) Repository 실행 및 DTO 변환
 */
@Service
@RequiredArgsConstructor
public class UnifiedSearchService {

    private final UnifiedSearchQueryRepository unifiedSearchQueryRepository;
    private final SearchIntentResolver searchIntentResolver;
    private final SearchRankingPolicy searchRankingPolicy;

    /**
     * 상세 검색 실행.
     */
    public Page<SearchResponseDTO> search(
            String keyword,
            String category,
            String order,
            Pageable pageable
    ) {
        String normalizedCategory = normalizeCategory(category);

        SearchIntentContext intentContext = searchIntentResolver.resolve(keyword);
        SearchQueryPlan plan = searchRankingPolicy.plan(intentContext, normalizedCategory, order);

        SearchHits<UnifiedSearchDocument> hits =
                unifiedSearchQueryRepository.search(plan, pageable);

        if (shouldFallbackToCategoryOnlyKeyword(hits, normalizedCategory, intentContext.normalizedKeyword())) {
            SearchQueryPlan fallbackPlan = withoutIntentExpansion(plan);
            hits = unifiedSearchQueryRepository.search(fallbackPlan, pageable);
        }

        List<SearchResponseDTO> content = hits.getSearchHits()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    /**
     * 카테고리 상세 검색에서 결과가 비어있으면, 과도한 의도 확장을 제거한 fallback 실행 여부를 판단한다.
     */
    private boolean shouldFallbackToCategoryOnlyKeyword(
            SearchHits<UnifiedSearchDocument> hits,
            String normalizedCategory,
            String normalizedKeyword
    ) {
        return normalizedCategory != null
                && normalizedKeyword != null
                && !normalizedKeyword.isBlank()
                && hits.getTotalHits() == 0;
    }

    /**
     * 의도 확장어 제약을 제거한 fallback 계획을 만든다.
     */
    private SearchQueryPlan withoutIntentExpansion(SearchQueryPlan plan) {
        return new SearchQueryPlan(
                plan.keyword(),
                plan.category(),
                plan.order(),
                List.of(),
                plan.categoryBoosts(),
                plan.negativeKeywords(),
                plan.negativeStrategy(),
                plan.negativeDownrankBoost(),
                plan.expansionTermBoost(),
                plan.autocompleteBoost(),
                plan.noticeTypeBoost(),
                plan.noticeGeneralCategoryBoost(),
                plan.intentRecencyWindowDays(),
                plan.freshnessRules(),
                plan.popularityRules()
        );
    }

    /**
     * ES hit -> 응답 DTO 변환.
     */
    private SearchResponseDTO toResponse(SearchHit<UnifiedSearchDocument> hit) {
        UnifiedSearchDocument doc = hit.getContent();

        SearchType searchType = SearchType.from(doc.getType());

        String highlightedTitle = extractHighlight(hit, "title", doc.getTitle());
        String highlightedContent = extractHighlight(hit, "content", doc.getContent());

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

    /** 통합 문서 id 규칙: TYPE:ORIGINAL_ID */
    private String buildUnifiedId(SearchType type, String originalId) {
        return type.name() + ":" + originalId;
    }

    /** highlight 값이 없으면 원문 fallback 반환. */
    private String extractHighlight(SearchHit<UnifiedSearchDocument> hit, String field, String fallback) {
        if (hit.getHighlightFields() == null) {
            return fallback;
        }

        List<String> highlights = hit.getHighlightFields().get(field);
        if (highlights == null || highlights.isEmpty()) {
            return fallback;
        }

        return highlights.get(0);
    }

    /** category 파라미터를 내부 SearchType enum 값으로 정규화. */
    private String normalizeCategory(String rawCategory) {
        SearchType parsed = SearchType.from(rawCategory);
        return parsed == null ? null : parsed.name();
    }
}
