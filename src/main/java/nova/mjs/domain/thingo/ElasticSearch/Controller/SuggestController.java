package nova.mjs.domain.thingo.ElasticSearch.Controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.DTO.SeasonalSuggestionResponse;
import nova.mjs.domain.thingo.ElasticSearch.Service.SeasonalSuggestService;
import nova.mjs.domain.thingo.ElasticSearch.Service.SuggestService;
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
    private final SeasonalSuggestService seasonalSuggestService;

    // 검색 자동완성 추천
    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(@RequestParam String keyword) {
        List<String> suggestions = suggestService.getSuggestions(keyword);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    // 시기별 추천 검색어 조회 (미운영 기간은 인기 검색어 반환)
    @GetMapping("/suggest/seasonal")
    public ResponseEntity<ApiResponse<SeasonalSuggestionResponse>> getSeasonalSuggestions(
            @RequestParam(required = false) Integer month
    ) {
        SeasonalSuggestionResponse response = seasonalSuggestService.getSeasonalSuggestions(month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
