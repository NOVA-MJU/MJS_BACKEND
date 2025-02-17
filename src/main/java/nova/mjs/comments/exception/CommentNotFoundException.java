package nova.mjs.comments.exception;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

import java.util.UUID;

public class CommentNotFoundException extends BusinessBaseException {

    public CommentNotFoundException(UUID commentUuid) {
        super("[MJS] 요청한 댓글을 찾을 수 없습니다. ID = " + commentUuid, ErrorCode.INVALID_REQUEST);
    }
}