package nova.mjs.notice.search;

import lombok.RequiredArgsConstructor;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.entity.Notice;
import nova.mjs.notice.repository.NoticeRepository;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notice/elasticsearch")
@RequiredArgsConstructor
public class NoticeSearchController {

    private final NoticeSearchService searchService;
    private final NoticeRepository noticeRepository;

    /**
     * ✅ 1. Elasticsearch 저장용 (DB → ES)
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<String>> saveNoticesToElasticsearch() {
        List<Notice> notices = noticeRepository.findAll();
        notices.forEach(searchService::saveNoticeToElasticsearch);

        return ResponseEntity.ok(ApiResponse.success("✅ Elasticsearch 저장 완료 (" + notices.size() + "건)"));
    }

    /**
     * ✅ 2. Elasticsearch 검색용
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeResponseDto>>> searchNotice(@RequestParam String keyword) {
        List<NoticeResponseDto> result = searchService.searchByKeyword(keyword);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
