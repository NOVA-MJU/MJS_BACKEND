package nova.mjs.domain.thingo.department.exception;

import nova.mjs.util.exception.ErrorCode;

public class CollegeNotFoundException extends DepartmentException {

    public CollegeNotFoundException() {
        super(ErrorCode.COLLEGE_NOT_FOUND);
    }

    public CollegeNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
