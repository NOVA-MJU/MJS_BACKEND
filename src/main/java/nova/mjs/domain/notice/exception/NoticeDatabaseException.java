package nova.mjs.domain.notice.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class NoticeDatabaseException extends BusinessBaseException {
    public NoticeDatabaseException() {
        super(ErrorCode.DATABASE_ERROR);
    }
}