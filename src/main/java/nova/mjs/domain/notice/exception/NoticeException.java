package nova.mjs.domain.notice.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class NoticeException extends BusinessBaseException {
    public NoticeException(ErrorCode errorCode) {
        super(errorCode);
    }
}