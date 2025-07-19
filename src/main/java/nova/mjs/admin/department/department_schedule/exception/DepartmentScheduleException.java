package nova.mjs.admin.department.department_schedule.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class DepartmentScheduleException extends BusinessBaseException {
    public DepartmentScheduleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DepartmentScheduleException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
