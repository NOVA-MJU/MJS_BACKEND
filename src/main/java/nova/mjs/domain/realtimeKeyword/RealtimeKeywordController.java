package nova.mjs.domain.realtimeKeyword;

import lombok.RequiredArgsConstructor;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/keywords")
public class RealtimeKeywordController {

    private final RealtimeKeywordService realtimeKeywordService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> searchKeyword(
            @RequestBody SearchKeywordRequestDTO request){

        realtimeKeywordService.recordSearch(request.getKeyword());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @GetMapping("/top10")
    public ResponseEntity<ApiResponse<List<String>>> topkeywords(
            @RequestParam(defaultValue = "10") int count){

        List<String> topKeywords = realtimeKeywordService.getTopKeywords(count);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(topKeywords));
    }
}
