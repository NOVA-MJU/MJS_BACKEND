package nova.mjs.notice.dto;

import lombok.*;
import nova.mjs.notice.entity.Notice;
import nova.mjs.notice.search.NoticeSearchDocument;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponseDto {
    private String title;        // 공지 제목
    private String date;         // 공지 날짜
    private String category;     // 공지 타입
    private String link;         // 공지 링크

    public static NoticeResponseDto noticeEntity(Notice notice) {
        return NoticeResponseDto.builder()
                .title(notice.getTitle())
                .date(notice.getDate())
                .category(notice.getCategory())
                .link(notice.getLink())
                .build();
    }

    public static NoticeResponseDto fromSearchDocument(NoticeSearchDocument document) {
        return NoticeResponseDto.builder()
                .title(document.getTitle())
                .date(document.getDate())
                .category(document.getCategory())
                .link(document.getLink())
                .build();
    }

}
