package nova.mjs.util.scheduler.exception;

import nova.mjs.util.exception.ErrorCode;

public class SchedulerUnknownException extends SchedulerException {

    public SchedulerUnknownException() {
        super(ErrorCode.SCHEDULER_UNKNOWN_ERROR);
    }

    public SchedulerUnknownException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
