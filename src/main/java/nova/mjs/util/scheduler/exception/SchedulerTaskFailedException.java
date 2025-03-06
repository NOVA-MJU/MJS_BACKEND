package nova.mjs.util.scheduler.exception;

import nova.mjs.util.exception.ErrorCode;

public class SchedulerTaskFailedException extends SchedulerException {

    public SchedulerTaskFailedException() {
        super(ErrorCode.SCHEDULER_TASK_FAILED);
    }

    public SchedulerTaskFailedException(ErrorCode errorCode) {
        super(errorCode);
    }
    public SchedulerTaskFailedException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
