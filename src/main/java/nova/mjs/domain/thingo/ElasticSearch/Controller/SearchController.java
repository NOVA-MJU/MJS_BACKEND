package nova.mjs.domain.thingo.ElasticSearch.Controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Service.SearchIndexSyncService;
import nova.mjs.domain.thingo.ElasticSearch.Service.UnifiedSearchService;
import nova.mjs.domain.thingo.ElasticSearch.SearchResponseDTO;
import nova.mjs.domain.thingo.realtimeKeyword.RealtimeKeywordService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SearchController
 *
 * - 검색 API의 단일 진입점
 * - 검색 로직은 UnifiedSearchService에 전적으로 위임
 * - Controller는 요청/응답 조립만 담당
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

    private final UnifiedSearchService unifiedSearchService;
    private final SearchIndexSyncService searchIndexSyncService;
    private final RealtimeKeywordService realtimeKeywordService;

    /**
     * Elasticsearch 전체 재색인
     * - 운영/관리 목적
     * - 성능 평가 대상 아님
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncElasticsearch() {
        searchIndexSyncService.syncAll();
        return ResponseEntity.ok(ApiResponse.success("Success Indexing"));
    }

    /**
     * 통합 검색 상세 조회
     */
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<Page<SearchResponseDTO>>> searchDetail(
            @RequestParam String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(name = "order", defaultValue = "relevance") String order,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<SearchResponseDTO> result =
                unifiedSearchService.search(keyword, type, order, pageable);

        realtimeKeywordService.recordSearch(keyword);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 통합 검색 Overview
     * - 타입별 Top N
     * - unified index만 사용
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, List<SearchResponseDTO>>>> searchOverview(
            @RequestParam String keyword,
            @RequestParam(name = "order", defaultValue = "relevance") String order,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize
    ) {
        Map<String, List<SearchResponseDTO>> result =
                unifiedSearchService.overview(keyword, order, pageSize);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
