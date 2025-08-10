package nova.mjs.domain.community.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nova.mjs.domain.community.entity.CommunityBoard;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 커뮤니티 게시글 응답 DTO
 */
public class CommunityBoardResponse {

    /* ========================== 요약 DTO ========================== */
    @Data
    @Builder
    @AllArgsConstructor
    public static class SummaryDTO {
        private UUID uuid;
        private String title;
        private String previewContent;
        private int viewCount;
        private Boolean published;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int likeCount;
        private int commentCount;
        private String author;
        private boolean isLiked;
        private boolean popular;

        public static SummaryDTO fromEntityPreview(CommunityBoard e,
                                                   int likeCnt, int cmtCnt, boolean liked) {
            return SummaryDTO.builder()
                    .uuid(e.getUuid())
                    .title(e.getTitle())
                    .previewContent(e.getPreviewContent())
                    .viewCount(e.getViewCount())
                    .published(e.getPublished())
                    .publishedAt(e.getPublishedAt())   // 그대로 반환
                    .createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt())
                    .likeCount(likeCnt)
                    .commentCount(cmtCnt)
                    .author(e.getAuthor() != null ? e.getAuthor().getNickname() : "Unknown")
                    .isLiked(liked)
                    .build();
        }

        public static SummaryDTO fromEntityPreview(CommunityBoard e,
                                                   int likeCnt, int cmtCnt, boolean liked, boolean popular) {
            return SummaryDTO.builder()
                    .uuid(e.getUuid())
                    .title(e.getTitle())
                    .previewContent(e.getPreviewContent())
                    .viewCount(e.getViewCount())
                    .published(e.getPublished())
                    .publishedAt(e.getPublishedAt())   // 그대로 반환
                    .createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt())
                    .likeCount(likeCnt)
                    .commentCount(cmtCnt)
                    .author(e.getAuthor() != null ? e.getAuthor().getNickname() : "Unknown")
                    .isLiked(liked)
                    .popular(popular)
                    .build();
        }
    }

/* ========================== 상세 DTO ========================== */
    @Data
    @Builder
    public static class DetailDTO {
        private UUID uuid;
        private String title;
        private String content;
        private String contentPreview;
        private int viewCount;
        private Boolean published;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int likeCount;
        private int commentCount;
        private String author;
        private boolean isLiked;

        public static DetailDTO fromEntity(CommunityBoard e,
                                           int likeCnt, int cmtCnt, boolean liked) {
            return DetailDTO.builder()
                    .uuid(e.getUuid())
                    .title(e.getTitle())
                    .content(e.getContent())
                    .contentPreview(e.getPreviewContent())
                    .viewCount(e.getViewCount())
                    .published(e.getPublished())
                    .publishedAt(e.getPublishedAt())   // atZone 제거, 그대로 사용
                    .createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt())
                    .likeCount(likeCnt)
                    .commentCount(cmtCnt)
                    .author(e.getAuthor() != null ? e.getAuthor().getNickname() : "Unknown")
                    .isLiked(liked)
                    .build();
        }
    }
}
