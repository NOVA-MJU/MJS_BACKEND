package nova.mjs.util.exception.request;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class RequestException extends BusinessBaseException {

    public RequestException(ErrorCode errorCode) {
        super(errorCode);
    }
    public RequestException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}