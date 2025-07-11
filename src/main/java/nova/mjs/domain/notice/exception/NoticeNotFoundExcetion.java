package nova.mjs.domain.notice.exception;

import nova.mjs.util.exception.ErrorCode;

public class NoticeNotFoundExcetion extends NoticeException {
    public NoticeNotFoundExcetion() {
        super(ErrorCode.NOTICE_NOT_FOUND);
    }
}