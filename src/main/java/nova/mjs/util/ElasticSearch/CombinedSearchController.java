package nova.mjs.util.ElasticSearch;

import lombok.RequiredArgsConstructor;
import nova.mjs.util.ElasticSearch.Document.SearchDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class CombinedSearchController {
    private final CombinedSearchService combinedSearchService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncElasticsearch() {
        combinedSearchService.syncAll();
        return ResponseEntity.ok("Elasticsearch 인덱싱 완료!");
    }

    @GetMapping("/detail")
    public ResponseEntity<List<SearchResponseDTO>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<SearchResponseDTO> results = combinedSearchService.unifiedSearch(keyword, type, page, size);
        return ResponseEntity.ok(results);
    }
}
