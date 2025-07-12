package nova.mjs.admin.account.exception;

import nova.mjs.util.exception.ErrorCode;

public class PasswordIsInvalidException extends AdminException {

    public PasswordIsInvalidException() {super(ErrorCode.PASSWORD_IS_INVALID);}
    public PasswordIsInvalidException(ErrorCode errorCode) {
        super(errorCode);
    }
    public PasswordIsInvalidException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
