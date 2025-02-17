package nova.mjs.comments.exception;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class CommentNotFoundException extends BusinessBaseException {

    public CommentNotFoundException(Long commentId) {
        super("[MJS] 요청한 댓글을 찾을 수 없습니다. ID = " + commentId, ErrorCode.INVALID_REQUEST);
    }
}