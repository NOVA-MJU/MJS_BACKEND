package nova.mjs.admin.exception;

import nova.mjs.util.exception.ErrorCode;

public class AdminIdMismatchException extends AdminException {
    public AdminIdMismatchException() {
        super(ErrorCode.ADMIN_ID_MISMATCH);
    }
    public AdminIdMismatchException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AdminIdMismatchException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
