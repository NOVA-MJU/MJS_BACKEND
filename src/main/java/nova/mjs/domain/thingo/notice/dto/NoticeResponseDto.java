package nova.mjs.domain.thingo.notice.dto;

import lombok.*;
import nova.mjs.domain.thingo.notice.entity.Notice;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 공지 응답 DTO
 */
public class NoticeResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {

        private String title;
        private LocalDateTime date;
        private String category;
        private String link;
        private Integer viewCount;

        public static Summary fromEntity(Notice notice) {
            return Summary.builder()
                    .title(notice.getTitle())
                    .date(notice.getDate())
                    .category(notice.getCategory())
                    .link(notice.getLink())
                    .viewCount(notice.getViewCount())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {

        private String title;
        private LocalDateTime date;
        private String category;
        private String link;
        private Integer viewCount;
        private String content;

        public static Detail fromEntity(Notice notice) {
            return Detail.builder()
                    .title(notice.getTitle())
                    .date(notice.getDate())
                    .category(notice.getCategory())
                    .link(notice.getLink())
                    .viewCount(notice.getViewCount())
                    .content(notice.getContent())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trending {
        private String title;
        private LocalDateTime date;
        private String category;
        private String link;
        private Integer viewCount;
        private Integer countView;
        private LocalDate countViewDate;

        public static Trending fromEntity(Notice notice) {
            return Trending.builder()
                    .title(notice.getTitle())
                    .date(notice.getDate())
                    .category(notice.getCategory())
                    .link(notice.getLink())
                    .viewCount(notice.getViewCount())
                    .countView(notice.getViewCountDeltaToday())
                    .countViewDate(notice.getViewCountDeltaDate())
                    .build();
        }
    }
}
