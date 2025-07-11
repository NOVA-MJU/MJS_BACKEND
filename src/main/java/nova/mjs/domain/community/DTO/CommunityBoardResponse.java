package nova.mjs.domain.community.DTO;

import lombok.Builder;
import lombok.Data;
import nova.mjs.domain.community.entity.CommunityBoard;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public class CommunityBoardResponse {

    @Data
    @Builder
    public static class SummaryDTO {
        private UUID uuid;
        private String title;
        private String previewContent;
        private List<String> contentImages;
        private int viewCount;
        private Boolean published;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int likeCount;
        private int commentCount;
        private String author;
        private boolean isLiked;

        public static SummaryDTO fromEntityPreview(CommunityBoard entity, int likeCount, int commentCount, boolean isLiked) {
            return SummaryDTO.builder()
                    .uuid(entity.getUuid())
                    .title(entity.getTitle())
                    .previewContent(entity.getPreviewContent())
                    .contentImages(entity.getContentImages())
                    .viewCount(entity.getViewCount())
                    .published(entity.getPublished())
                    .publishedAt(entity.getPublishedAt())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .author(entity.getAuthor() != null ? entity.getAuthor().getNickname() : "Unknown")
                    .isLiked(isLiked)
                    .build();
        }
    }

    @Data
    @Builder
    public static class DetailDTO {
        private UUID uuid;
        private String title;
        private String content;
        private List<String> contentImages;
        private int viewCount;
        private Boolean published;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int likeCount;
        private int commentCount;
        private String author;
        private boolean isLiked;

        public static DetailDTO fromEntity(CommunityBoard entity, int likeCount, int commentCount, boolean isLiked) {
            LocalDateTime seoulPublishedAt = entity.getPublishedAt() != null
                    ? entity.getPublishedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
                    : null;

            return DetailDTO.builder()
                    .uuid(entity.getUuid())
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .contentImages(entity.getContentImages())
                    .viewCount(entity.getViewCount())
                    .published(entity.getPublished())
                    .publishedAt(seoulPublishedAt)
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .author(entity.getAuthor() != null ? entity.getAuthor().getNickname() : "Unknown")
                    .isLiked(isLiked)
                    .build();
        }

    }
}
