package nova.mjs.domain.thingo.notice.exception;

import nova.mjs.util.exception.ErrorCode;

public class NoticeNotFoundException extends NoticeException {
    public NoticeNotFoundException() {
        super(ErrorCode.NOTICE_NOT_FOUND);
    }
}