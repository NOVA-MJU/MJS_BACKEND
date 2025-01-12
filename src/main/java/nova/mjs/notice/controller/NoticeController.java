package nova.mjs.notice.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.notice.service.NoticeService;
import nova.mjs.notice.dto.NoticeResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    // 공지사항 가져오기
    @GetMapping("/api/v1/notice")
    public List<NoticeResponseDto> getNotices(
            @RequestParam("type") String type) { // 공지 종류
        return noticeService.fetchNotices(type);
    }
}