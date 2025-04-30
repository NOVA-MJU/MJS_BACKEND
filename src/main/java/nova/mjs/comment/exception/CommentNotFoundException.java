package nova.mjs.comment.exception;
import lombok.extern.log4j.Log4j2;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

@Log4j2
public class CommentNotFoundException extends BusinessBaseException {
    public CommentNotFoundException() {
        super(ErrorCode.INVALID_REQUEST);
    }
}