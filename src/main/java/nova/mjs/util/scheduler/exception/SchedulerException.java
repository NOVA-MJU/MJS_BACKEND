package nova.mjs.util.scheduler.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class SchedulerException extends BusinessBaseException {

    public SchedulerException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SchedulerException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
