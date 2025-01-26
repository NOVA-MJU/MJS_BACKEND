package nova.mjs.notice.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.notice.service.NoticeCrawlingService;
import nova.mjs.notice.service.NoticeService;
import nova.mjs.notice.dto.NoticeResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeCrawlingService noticeCrawlingServiceService;
    private final NoticeService noticeService;

    // 공지사항 크롤링 가져오기
    @GetMapping("/api/v1/notice/crawl")
    public List<NoticeResponseDto> fetchNotices(
            @RequestParam("type") String type) { // 공지 종류
        return noticeCrawlingServiceService.fetchNotices(type);
    }

    // DB에서 공지사항 조회

    /**
     * 공지사항 조회 API
     *
     * @param category 공지 카테고리
     * @param year 조회할 연도 (선택)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지당 항목 수 (기본값: 15)
     * @return 공지사항 리스트
     */
    @GetMapping("/api/v1/notice")
    public List<NoticeResponseDto> getNotices(
            @RequestParam(value = "category") String category,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "15") int size
    ) {
        return noticeService.getNotices(category, year, page, size);
    }
}