package nova.mjs.notice.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.notice.service.NoticeCrawlingService;
import nova.mjs.notice.service.NoticeService;
import nova.mjs.notice.dto.NoticeResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeCrawlingService noticeCrawlingServiceService;
    private final NoticeService noticeService;

    // 공지사항 크롤링 가져오기
    @PostMapping ("/api/v1/notice/crawl")
    public List<NoticeResponseDto> fetchNotices(
            @RequestParam("type") String type) { // 공지 종류
        return noticeCrawlingServiceService.fetchNotices(type);
    }

    // (2) DB에서 공지사항 조회
    @GetMapping("/api/v1/notice")
    public List<NoticeResponseDto> getNotices(
            @RequestParam(value = "category") String category,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "15") int size,
            @RequestParam(value = "sort", required = false, defaultValue = "desc") String sort
    ) {
        // Service로 넘기면서 정렬 방식도 함께 전달
        return noticeService.getNotices(category, year, page, size, sort);
    }
}