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
    private UUID uuid;                  // 외부적으로 사용할 UUID
    private String title;               // 게시글 제목
    private String content;             // 게시글 내용
    private List<String> contentImages; // 게시글 이미지 URL 리스트
    private int viewCount;              // 조회 수
    private Boolean published;          // 게시글 공개 여부
    private LocalDateTime publishedAt;  // 게시 시간
    private LocalDateTime createdAt;  // 게시 시간
    private LocalDateTime updatedAt;  // 게시 시간
    private int likeCount; // 좋아요 개수 추가
    private String author;
    private int commentCount;

    // 엔티티에서 DTO로 변환하는 메서드
    public static CommunityBoardResponse fromEntity(CommunityBoard entity, int likeCount, int commentCount) {

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
                .likeCount(likeCount)
                .commentCount(commentCount)
                .author(entity.getAuthor() != null ? entity.getAuthor().getNickname() : "Unknown") // ✅ 작성자 닉네임 처리
                .build();
    }
}