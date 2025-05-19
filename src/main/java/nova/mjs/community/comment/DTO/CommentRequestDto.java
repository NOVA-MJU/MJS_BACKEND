package nova.mjs.community.comment.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {
    /** 게시글 본문 내용 */
    private String content;

}
