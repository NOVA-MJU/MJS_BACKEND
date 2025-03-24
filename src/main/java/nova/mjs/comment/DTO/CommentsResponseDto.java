package nova.mjs.comment.DTO;
import lombok.*;
import nova.mjs.comment.entity.Comment;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsResponseDto {
    private UUID communityBoardUuid;       // 댓글이 작성된 게시글의 uuid
    private List<CommentSummaryDto> comments;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CommentSummaryDto {
        private UUID commentUUID;
        private String content;
        private String nickname;
        private int likes;

        public static CommentSummaryDto fromEntity(Comment comment) {
            return CommentSummaryDto.builder()
                    .commentUUID(comment.getUuid())
                    .content(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .likes(comment.getLikes())
                    .build();
        }
    }

    // Entity 리스트 -> DTO 변환 (게시글의 모든 댓글)
    public static CommentsResponseDto fromEntities(UUID communityBoardUuid, List<Comment> comments) {
        List<CommentSummaryDto> commentList = comments.stream()
                .map(CommentSummaryDto::fromEntity)
                .toList();

        return CommentsResponseDto.builder()
                .communityBoardUuid(communityBoardUuid)
                .comments(commentList)
                .build();
    }

    //Entity 하나 -> DTO 변환 (단일 댓글 조회)
    public static CommentsResponseDto fromEntity(Comment comment) {
        return CommentsResponseDto.builder()
                .communityBoardUuid(comment.getCommunityBoard().getUuid())
                .comments(List.of(CommentSummaryDto.fromEntity(comment)))
                .build();
    }

    public Comment toEntity(CommunityBoard communityBoard, Member member) {
        return Comment.builder()
                .communityBoard(communityBoard)
                .member(member)
                .content(this.comments.get(0).getContent()) // 요청 받은 content 사용
                .likes(0) // 새 댓글은 좋아요 수 0
                .uuid(UUID.randomUUID()) // 댓글 UUID 자동 생성
                .build();
    }


}
