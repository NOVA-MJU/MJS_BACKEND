package nova.mjs.comments.exception;
import lombok.extern.log4j.Log4j2;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

import java.util.UUID;

@Log4j2
public class CommentNotFoundException extends BusinessBaseException {
    public CommentNotFoundException(UUID commentUuid) {
        super(ErrorCode.INVALID_REQUEST);
        log.error("[MJS] 요청한 댓글을 찾을 수 없습니다. ID = {}", commentUuid);
    }
}