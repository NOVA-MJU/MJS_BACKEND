package nova.mjs.domain.community.comment.DTO;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import nova.mjs.domain.community.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private UUID communityBoardUuid;       // 댓글이 작성된 게시글의 uuid
    private List<CommentSummaryDto> comments;

    @JsonPropertyOrder({
            "commentUUID",
            "content",
            "nickname",
            "likeCount",
            "createdAt",
            "liked",
            "replies"
    })
    @Getter
    @Builder
    @AllArgsConstructor
    public static class CommentSummaryDto {
        private UUID commentUUID;
        private String content;
        private String nickname;
        private int likeCount;
        private LocalDateTime createdAt;
        private boolean isLiked; // 현재 로그인 한 사용자가 좋아요를 눌렀는가 T/F

        private List<CommentSummaryDto> replies;

        public static CommentSummaryDto fromEntity(Comment comment, boolean isLiked) {
            return CommentSummaryDto.builder()
                    .commentUUID(comment.getUuid())
                    .content(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .likeCount(comment.getLikeCount())
                    .isLiked(isLiked)
                    .createdAt(comment.getCreatedAt())
                    .build();
        }

        public static CommentSummaryDto fromEntity(Comment comment) {
            return CommentSummaryDto.builder()
                    .commentUUID(comment.getUuid())
                    .content(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .likeCount(comment.getLikeCount())
                    .createdAt(comment.getCreatedAt())
                    .build();
        }
        // "부모 댓글"을 DTO로 변환하되, 자식 목록도 함께 변환하는 메서드
        public static CommentSummaryDto fromEntityWithReplies(Comment comment, boolean isLiked, Set<UUID> likedSet) {
            // 1) 부모 댓글의 기본 정보
            CommentSummaryDto.CommentSummaryDtoBuilder builder = CommentSummaryDto.builder()
                    .commentUUID(comment.getUuid())
                    .content(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .likeCount(comment.getLikeCount())
                    .createdAt(comment.getCreatedAt())
                    .isLiked(isLiked);

            // 2) 자식 댓글(대댓글) 변환
            //    depth=1만 허용 → 자식의 자식은 처리 안 함
            List<CommentSummaryDto> replyDtos = comment.getReplies().stream()
                    .map(child -> {
                        boolean childIsLiked = (likedSet != null && likedSet.contains(child.getUuid()));
                        return fromEntityNoReplies(child, childIsLiked);
                    })
                    .toList();

            builder.replies(replyDtos);
            return builder.build();
        }

        // "자식 댓글"은 더 이상의 자식 목록을 보지 않는다고 가정(depth=1)
        public static CommentSummaryDto fromEntityNoReplies(Comment comment, boolean isLiked) {
            return CommentSummaryDto.builder()
                    .commentUUID(comment.getUuid())
                    .content(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .likeCount(comment.getLikeCount())
                    .createdAt(comment.getCreatedAt())
                    .isLiked(isLiked)
                    .replies(List.of()) // 자식은 depth=1까지만
                    .build();
        }

    }
}
/*
    // Entity 리스트 -> DTO 변환 (게시글의 모든 댓글)
    public static CommentResponseDto fromEntities(UUID communityBoardUuid, List<Comment> comments) {
        List<CommentSummaryDto> commentList = comments.stream()
                .map(CommentSummaryDto::fromEntity)
                .toList();

        return CommentResponseDto.builder()
                .communityBoardUuid(communityBoardUuid)
                .comments(commentList)
                .build();
    }

    //Entity 하나 -> DTO 변환 (단일 댓글 조회)
    public static CommentResponseDto fromEntity(Comment comment) {
        return CommentResponseDto.builder()
                .communityBoardUuid(comment.getCommunityBoard().getUuid())
                .comments(List.of(CommentSummaryDto.fromEntity(comment)))
                .build();
    }

    public Comment toEntity(CommunityBoard communityBoard, Member member) {
        return Comment.builder()
                .communityBoard(communityBoard)
                .member(member)
                .content(this.comments.get(0).getContent()) // 요청 받은 content 사용
                .likeCount(0) // 새 댓글은 좋아요 수 0
                .uuid(UUID.randomUUID()) // 댓글 UUID 자동 생성
                .build();
    }
*/


