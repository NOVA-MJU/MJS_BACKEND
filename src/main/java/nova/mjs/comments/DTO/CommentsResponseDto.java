package nova.mjs.comments.DTO;
import lombok.*;
import nova.mjs.comments.entity.Comments;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsResponseDto {
    private UUID communityBoardUuid;       // 댓글이 작성된 게시글의 uuid
    private String nickname;           // 작성자 닉네임
    private String content;            // 내용
    private int likes;                 // 좋아요 수
    private LocalDateTime createDate;  // 작성된 날짜

    public static CommentsResponseDto fromEntity(Comments entity) {
        return CommentsResponseDto.builder()
                .communityBoardUuid(entity.getCommunity_board().getUuid())
                .nickname(entity.getMember().getNickname())
                .content(entity.getContent())
                .likes(entity.getLikes())
                .createDate(entity.getCreateDate())
                .build();
    }

    public Comments toEntity(CommunityBoard communityBoard, Member member) {
        return Comments.builder()
                .community_board(communityBoard)
                .member(member)
                .content(this.content)
                .likes(this.likes)
                .createDate(this.createDate != null ? this.createDate : LocalDateTime.now()) // 없으면 현재시간 사용
                .build();
    }
}
