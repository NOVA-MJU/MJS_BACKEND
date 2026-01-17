package nova.mjs.domain.notice.dto;

import lombok.*;
import nova.mjs.domain.notice.entity.Notice;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponseDto {
    private String title;        // 공지 제목
    private LocalDateTime date;         // 공지 날짜
    private String content; // 본문
    private String category;     // 공지 타입
    private String link;         // 공지 링크

    public static NoticeResponseDto noticeEntity(Notice notice) {
        return NoticeResponseDto.builder()
                .title(notice.getTitle())
                .date(notice.getDate())
                .content(notice.getContent())
                .category(notice.getCategory())
                .link(notice.getLink())
                .build();
    }

}
