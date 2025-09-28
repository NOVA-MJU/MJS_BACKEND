package nova.mjs.domain.calendar.exception;

import nova.mjs.domain.department.exception.DepartmentException;
import nova.mjs.util.exception.ErrorCode;

public class CalendarInvalidInputException extends DepartmentException {
    public CalendarInvalidInputException() {
        super(ErrorCode.INVALID_PARAM_REQUEST);
    }
}