package nova.mjs.member.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CommentWithBoardResponse {

    // 게시물 정보
    private UUID boardUuid;
    private String boardTitle;
    private String boardPreviewContent;

    // 댓글 정보
    private UUID commentUuid;
    private String commentPreviewContent;
}
