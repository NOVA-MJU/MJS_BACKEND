package nova.mjs.department.exception;

import nova.mjs.util.exception.ErrorCode;

public class DepartmentNotFoundException extends DepartmentException {

    public DepartmentNotFoundException() {
        super(ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    public DepartmentNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
