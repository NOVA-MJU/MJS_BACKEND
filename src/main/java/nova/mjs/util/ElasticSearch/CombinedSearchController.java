package nova.mjs.util.ElasticSearch;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.realtimeKeyword.RealtimeKeywordService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class CombinedSearchController {
    private final CombinedSearchService combinedSearchService;
    private final RealtimeKeywordService realtimeKeywordService;

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncElasticsearch() {
        combinedSearchService.syncAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Success Indexing"));
    }

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<List<SearchResponseDTO>>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<SearchResponseDTO> results = combinedSearchService.unifiedSearch(keyword, type, page, size);

        realtimeKeywordService.recordSearch(keyword);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(results));
    }
}
