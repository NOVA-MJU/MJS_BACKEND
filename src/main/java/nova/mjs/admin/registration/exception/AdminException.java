package nova.mjs.admin.registration.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class AdminException  extends BusinessBaseException {
    public AdminException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AdminException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}