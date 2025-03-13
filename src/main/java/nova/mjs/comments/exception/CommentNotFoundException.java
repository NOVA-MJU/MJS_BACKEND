package nova.mjs.comments.exception;
import lombok.extern.log4j.Log4j2;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

import java.util.UUID;

@Log4j2
public class CommentNotFoundException extends BusinessBaseException {
    public CommentNotFoundException() {
        super(ErrorCode.INVALID_REQUEST);
    }
}