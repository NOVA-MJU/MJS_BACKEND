package nova.mjs.domain.mentorship.ElasticSearch.Controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.ElasticSearch.Service.SuggestService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SuggestController {
    private final SuggestService suggestService;

    // 검색 자동완성 추천
    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(@RequestParam String keyword) {
        List<String> suggestions = suggestService.getSuggestions(keyword);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}
