package nova.mjs.admin.account.exception;

import nova.mjs.util.exception.ErrorCode;

public class AdminIdMismatchWithDepartmentException extends AdminException {
    public AdminIdMismatchWithDepartmentException() {
        super(ErrorCode.ADMIN_ID_MISMATCH_WITH_DEPARTMENT_EXCEPTION);
    }
    public AdminIdMismatchWithDepartmentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AdminIdMismatchWithDepartmentException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
