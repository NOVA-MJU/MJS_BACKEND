package nova.mjs.admin.account.exception;

import nova.mjs.util.exception.ErrorCode;

public class InvalidRequestException extends AdminException {
    public InvalidRequestException() {
        super(ErrorCode.INVALID_REQUEST);
    }
    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidRequestException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
