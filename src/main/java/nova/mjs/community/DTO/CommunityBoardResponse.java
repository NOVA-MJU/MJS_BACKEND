package nova.mjs.community.DTO;

import lombok.Builder;
import lombok.Data;
import nova.mjs.community.entity.CommunityBoard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CommunityBoardResponse {
    private UUID uuid;
    private String title;
    private String content;
    private List<String> contentImages;
    private int viewCount;
    private Boolean published;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String author;

    public static CommunityBoardResponse fromEntity(CommunityBoard entity) {
        return CommunityBoardResponse.builder()
                .uuid(entity.getUuid())
                .title(entity.getTitle())
                .content(entity.getContent())
                .contentImages(entity.getContentImages())
                .viewCount(entity.getViewCount())
                .published(entity.getPublished())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .author(entity.getAuthor() != null ? entity.getAuthor().getNickname() : "Unknown") // ✅ 작성자 닉네임 처리
                .build();
    }
}