package nova.mjs.domain.thingo.notice.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.notice.service.NoticeCrawlingService;
import nova.mjs.domain.thingo.notice.service.NoticeService;
import nova.mjs.domain.thingo.notice.dto.NoticeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController {
    private final NoticeCrawlingService noticeCrawlingServiceService;
    private final NoticeService noticeService;

    // 공지사항 크롤링 가져오기
    @PostMapping ("/crawl")
    public List<NoticeResponseDto> fetchNotices(
            @RequestParam("type") String type) { // 공지 종류
        return noticeCrawlingServiceService.fetchNotices(type);
    }

    // (2) DB에서 공지사항 조회
    @GetMapping
    public Page<NoticeResponseDto> getNotices(
            @RequestParam(value = "category") String category,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            @RequestParam(value = "sort", defaultValue = "desc") String sort
    ) {
        return noticeService.getNotices(category, year, page, size, sort);
    }

    // (3) 전체 공지 크롤링 (모든 카테고리)
    @PostMapping("/crawl/all")
    public List<NoticeResponseDto> fetchAllNotices() {
        return noticeCrawlingServiceService.fetchAllNotices();
    }

}