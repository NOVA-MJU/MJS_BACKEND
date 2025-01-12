package nova.mjs.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeResponseDto {
    private String title; // 공지 제목
    private String date;  // 공지 날짜
    private String category; // 공지 타입
    private String fetchedDate; // 크롤링 수행 날짜
    private String link;
}
