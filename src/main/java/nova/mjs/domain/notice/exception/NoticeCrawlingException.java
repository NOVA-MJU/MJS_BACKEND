package nova.mjs.domain.notice.exception;

import lombok.Getter;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

@Getter
public class NoticeCrawlingException extends BusinessBaseException {

    public NoticeCrawlingException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public NoticeCrawlingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
