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

    @GetMapping
    public ResponseEntity<List<SearchDocument>> search(@RequestParam String keyword) {
        List<SearchDocument> results = combinedSearchService.unifiedSearch(keyword);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncElasticsearch() {
        combinedSearchService.syncAll();
        return ResponseEntity.ok("Elasticsearch 인덱싱 완료!");
    }

}
