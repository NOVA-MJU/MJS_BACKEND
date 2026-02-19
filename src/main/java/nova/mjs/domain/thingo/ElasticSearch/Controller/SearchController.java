package nova.mjs.domain.thingo.ElasticSearch.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.SearchResponseDTO;
import nova.mjs.domain.thingo.ElasticSearch.Service.SearchIndexSyncService;
import nova.mjs.domain.thingo.ElasticSearch.Service.UnifiedSearchService;
import nova.mjs.domain.thingo.ElasticSearch.search.SearchRankingPolicyStore;
import nova.mjs.domain.thingo.realtimeKeyword.RealtimeKeywordService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * SearchController
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

    private static final String SESSION_QUERY_HISTORY_KEY = "search.query.history";
    private static final int SESSION_HISTORY_LIMIT = 5;

    private final UnifiedSearchService unifiedSearchService;
    private final SearchIndexSyncService searchIndexSyncService;
    private final RealtimeKeywordService realtimeKeywordService;
    private final SearchRankingPolicyStore searchRankingPolicyStore;

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncElasticsearch() {
        searchIndexSyncService.syncAll();
        return ResponseEntity.ok(ApiResponse.success("Success Indexing"));
    }

    @GetMapping("/policy")
    public ResponseEntity<ApiResponse<SearchRankingPolicyStore.SearchRankingPolicySnapshot>> getPolicy() {
        return ResponseEntity.ok(ApiResponse.success(searchRankingPolicyStore.snapshot()));
    }

    @PostMapping("/policy")
    public ResponseEntity<ApiResponse<SearchRankingPolicyStore.SearchRankingPolicySnapshot>> updatePolicy(
            @RequestBody SearchRankingPolicyStore.SearchRankingPolicySnapshot request
    ) {
        SearchRankingPolicyStore.SearchRankingPolicySnapshot updated = searchRankingPolicyStore.upsert(request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<Page<SearchResponseDTO>>> searchDetail(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(name = "order", defaultValue = "relevance") String order,
            @PageableDefault(size = 10) Pageable pageable,
            HttpSession session
    ) {
        List<String> sessionContextQueries = getSessionContextQueries(session);

        Page<SearchResponseDTO> result =
                unifiedSearchService.search(keyword, type, order, pageable, sessionContextQueries);

        realtimeKeywordService.recordSearch(keyword);
        updateSessionContext(session, keyword);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @SuppressWarnings("unchecked")
    private List<String> getSessionContextQueries(HttpSession session) {
        Object attr = session.getAttribute(SESSION_QUERY_HISTORY_KEY);
        if (!(attr instanceof Deque<?> deque)) {
            return List.of();
        }

        return ((Deque<String>) deque).stream().toList();
    }

    @SuppressWarnings("unchecked")
    private void updateSessionContext(HttpSession session, String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        if (normalized.isBlank()) {
            return;
        }

        Object attr = session.getAttribute(SESSION_QUERY_HISTORY_KEY);
        Deque<String> deque;

        if (attr instanceof Deque<?>) {
            deque = (Deque<String>) attr;
        } else {
            deque = new ArrayDeque<>();
        }

        deque.remove(normalized);
        deque.addFirst(normalized);

        while (deque.size() > SESSION_HISTORY_LIMIT) {
            deque.removeLast();
        }

        session.setAttribute(SESSION_QUERY_HISTORY_KEY, new ArrayDeque<>(deque));
    }
}
