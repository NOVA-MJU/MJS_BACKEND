package nova.mjs.domain.member.DTO;

import lombok.Builder;
import lombok.Data;
import nova.mjs.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CommentWithBoardResponse {
    // 게시글 정보
    private UUID boardUuid;
    private String boardTitle;
    private String boardPreviewContent;
    private int boardCommentCount;
    private Boolean boardPublished;
    private LocalDateTime boardCreatedAt;
    private int boardLikeCount;
    private boolean boardIsLiked;
    private String author;


    // 댓글 정보
    private UUID commentUuid;
    private String commentPreviewContent;
    private int commentLikeCount;
    private boolean commentIsLiked;
    private LocalDateTime commentCreatedAt;

}
