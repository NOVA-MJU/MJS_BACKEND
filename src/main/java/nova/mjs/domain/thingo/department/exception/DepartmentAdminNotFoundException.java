package nova.mjs.domain.thingo.department.exception;

import nova.mjs.util.exception.ErrorCode;

public class DepartmentAdminNotFoundException extends DepartmentException {

    public DepartmentAdminNotFoundException() {
        super(ErrorCode.DEPARTMENT_ADMIN_NOT_FOUND);
    }

    public DepartmentAdminNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
