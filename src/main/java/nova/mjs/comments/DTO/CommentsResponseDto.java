package nova.mjs.comments.DTO;
import lombok.*;
import nova.mjs.comments.entity.Comments;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.repository.util.ReactiveWrapperConverters.map;

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

        public static CommentSummaryDto fromEntity(Comments comment) {
            return CommentSummaryDto.builder()
                    .commentUUID(comment.getUuid())
                    .content(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .likes(comment.getLikes())
                    .build();
        }
    }

    // Entity 리스트 -> DTO 변환 (게시글의 모든 댓글)
    public static CommentsResponseDto fromEntities(UUID communityBoardUuid, List<Comments> comments) {
        List<CommentSummaryDto> commentList = comments.stream()
                .map(CommentSummaryDto::fromEntity)
                .toList();

        return CommentsResponseDto.builder()
                .communityBoardUuid(communityBoardUuid)
                .comments(commentList)
                .build();
    }

    //Entity 하나 -> DTO 변환 (단일 댓글 조회)
    public static CommentsResponseDto fromEntity(Comments comment) {
        return CommentsResponseDto.builder()
                .communityBoardUuid(comment.getCommunity_board().getUuid())
                .comments(List.of(CommentSummaryDto.fromEntity(comment)))
                .build();
    }

    public Comments toEntity(CommunityBoard communityBoard, Member member) {
        return Comments.builder()
                .community_board(communityBoard)
                .member(member)
                .content(this.comments.get(0).getContent()) // 첫 번째 댓글 내용 가져오기
                .likes(this.comments.get(0).getLikes()) // 첫 번째 댓글 좋아요 수 가져오기
                .createDate(LocalDateTime.now())
                .uuid(UUID.randomUUID()) // 댓글 UUID 자동 생성
                .build();
    }


}
