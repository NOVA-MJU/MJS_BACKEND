package nova.mjs.util.scheduler.exception;

import nova.mjs.util.exception.ErrorCode;

public class SchedulerCronInvalidException extends SchedulerException {

    public SchedulerCronInvalidException() {
        super(ErrorCode.SCHEDULER_CRON_INVALID);
    }

    public SchedulerCronInvalidException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
