package nova.mjs.admin.department.department_schedule.exception;

import nova.mjs.util.exception.ErrorCode;

public class DepartmentScheduleNotFoundException extends DepartmentScheduleException{
    public DepartmentScheduleNotFoundException() {
        super(ErrorCode.DEPARTMENT_SCHEDULE_NOT_FOUND);
    }

    public DepartmentScheduleNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
