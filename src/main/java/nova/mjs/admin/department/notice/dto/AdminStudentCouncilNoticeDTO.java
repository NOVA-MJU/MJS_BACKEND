package nova.mjs.admin.department.notice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.department.entity.StudentCouncilNotice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminStudentCouncilNoticeDTO {

    /* ==========================================================
     * 요청
     * ========================================================== */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String title;
        private String content;


        @Builder.Default
        private List<String> imageUrls = new ArrayList<>();
    }


    /* ==========================================================
     * 응답
     * ========================================================== */
    @Data
    @Builder
    public static class Response {

        private UUID uuid;
        private String title;
        private String content;
        private String authorNickname;
        private LocalDateTime publishedAt;
        private String thumbnailUrl;
        private List<String> imageUrls;

        public static Response fromEntity(StudentCouncilNotice notice) {
            return Response.builder()
                    .uuid(notice.getUuid())
                    .title(notice.getTitle())
                    .content(notice.getContent())
                    .authorNickname(notice.getAuthorNickname())
                    .publishedAt(notice.getPublishedAt())
                    .thumbnailUrl(notice.getThumbnailUrl())
                    .imageUrls(
                            notice.getImages()
                                    .stream()
                                    .map(img -> img.getImageUrl())
                                    .toList()
                    )
                    .build();
        }
    }
}
