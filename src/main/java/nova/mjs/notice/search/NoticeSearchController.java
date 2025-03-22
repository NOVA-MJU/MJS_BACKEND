package nova.mjs.notice.search;

import lombok.RequiredArgsConstructor;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notice/elasticsearch")
@RequiredArgsConstructor
public class NoticeSearchController {

    private final NoticeSearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeResponseDto>>> searchNotice(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchByKeyword(keyword)));
    }

}
