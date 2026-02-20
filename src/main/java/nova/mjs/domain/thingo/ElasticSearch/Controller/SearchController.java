package nova.mjs.domain.thingo.ElasticSearch.Controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.SearchResponseDTO;
import nova.mjs.domain.thingo.ElasticSearch.Service.SearchIndexSyncService;
import nova.mjs.domain.thingo.ElasticSearch.Service.UnifiedSearchService;
import nova.mjs.domain.thingo.realtimeKeyword.RealtimeKeywordService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 검색 API 진입점.
 *
 * 역할:
 * - 요청 파라미터 수집
 * - 서비스 호출
 * - 공통 ApiResponse 래핑
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

    private final UnifiedSearchService unifiedSearchService;
    private final SearchIndexSyncService searchIndexSyncService;
    private final RealtimeKeywordService realtimeKeywordService;

    /**
     * 통합 인덱스 전체 동기화.
     * 운영/관리 목적 엔드포인트.
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncElasticsearch() {
        searchIndexSyncService.syncAll();
        return ResponseEntity.ok(ApiResponse.success("Success Indexing"));
    }

    /**
     * 검색 상세 조회.
     *
     * @param keyword  사용자 검색어
     * @param type     검색 타입 탭 (NOTICE | COMMUNITY | NEWS ...)
     * @param category 세부 카테고리 (예: notice-general)
     * @param order    relevance | latest | oldest
     * @param pageable 페이지 정보
     */
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<Page<SearchResponseDTO>>> searchDetail(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(name = "order", defaultValue = "relevance") String order,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<SearchResponseDTO> result =
                unifiedSearchService.search(keyword, type, category, order, pageable);

        realtimeKeywordService.recordSearch(keyword);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
