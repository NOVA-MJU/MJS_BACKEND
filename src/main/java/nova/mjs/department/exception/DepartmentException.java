package nova.mjs.department.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class DepartmentException extends BusinessBaseException {

    public DepartmentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DepartmentException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
