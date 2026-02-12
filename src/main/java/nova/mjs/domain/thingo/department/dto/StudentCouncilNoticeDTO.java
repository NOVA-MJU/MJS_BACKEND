package nova.mjs.domain.thingo.department.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.department.entity.StudentCouncilNotice;
import nova.mjs.domain.thingo.department.entity.StudentCouncilNoticeImage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class StudentCouncilNoticeDTO {

    /* ==========================================================
     * 목록
     * ========================================================== */
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class Summary {

        private final UUID noticeUuid;
        private final String title;
        private final String thumbnailUrl;
        private final String authorNickname;
        private final LocalDateTime publishedAt;

        public static Summary fromEntity(StudentCouncilNotice n) {
            return Summary.builder()
                    .noticeUuid(n.getUuid())
                    .title(n.getTitle())
                    .thumbnailUrl(n.getThumbnailUrl())
                    .authorNickname(n.getAuthorNickname())
                    .publishedAt(n.getPublishedAt())
                    .build();
        }
    }

    /* ==========================================================
     * 상세
     * ========================================================== */
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class Detail {

        private final UUID noticeUuid;
        private final String title;
        private final String content;
        private final String authorNickname;
        private final LocalDateTime publishedAt;
        private final List<String> imageUrls;

        public static Detail fromEntity(StudentCouncilNotice n) {
            return Detail.builder()
                    .noticeUuid(n.getUuid())
                    .title(n.getTitle())
                    .content(n.getContent())
                    .authorNickname(n.getAuthorNickname())
                    .publishedAt(n.getPublishedAt())
                    .imageUrls(
                            n.getImages()
                                    .stream()
                                    .map(StudentCouncilNoticeImage::getImageUrl)
                                    .toList()
                    )
                    .build();
        }
    }
}
