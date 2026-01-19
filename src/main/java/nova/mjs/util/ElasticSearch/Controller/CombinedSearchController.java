package nova.mjs.util.ElasticSearch.Controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.util.ElasticSearch.Service.CombinedSearchService;
import nova.mjs.util.ElasticSearch.SearchResponseDTO;
import nova.mjs.domain.thingo.realtimeKeyword.RealtimeKeywordService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class CombinedSearchController {
    private final CombinedSearchService combinedSearchService;
    private final RealtimeKeywordService realtimeKeywordService;

    // 동기화
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncElasticsearch() {
        combinedSearchService.syncAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Success Indexing"));
    }

    // type별로 검색
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<Page<SearchResponseDTO>>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(name = "order", required = false, defaultValue = "relevance") String order, // relevance(null ok) | latest | oldest
            @PageableDefault(size = 10)  Pageable pageable) {

        Page<SearchResponseDTO> results = combinedSearchService.unifiedSearch(keyword, type, order, pageable);

        realtimeKeywordService.recordSearch(keyword);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(results));
    }

    // 통합검색 페이지에서 타입별로 사용자가 지정한 상위 top 갯수만 보여줌
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, List<SearchResponseDTO>>>> searchOverview(
            @RequestParam String keyword,
            @RequestParam(name = "order", required = false, defaultValue = "relevance") String order,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5" ) Integer pageSize) {


        Map<String, List<SearchResponseDTO>> result = combinedSearchService.searchTop5EachType(keyword, order, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
