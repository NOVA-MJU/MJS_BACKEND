package nova.mjs.comments.DTO;
import lombok.*;
import nova.mjs.comments.entity.Comments;
import nova.mjs.member.Member;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsResponseDto {
    private String nickname;
    private String content;     // 내용
    private int likes;          // 좋아요 수

    public CommentsResponseDto commentsEntity(Comments entity) {
        return CommentsResponseDto.builder()
                .nickname(entity.getMember().getNickname())
                .content(entity.getContent())
                .likes(entity.getLikes())
                .build();
    }
}
