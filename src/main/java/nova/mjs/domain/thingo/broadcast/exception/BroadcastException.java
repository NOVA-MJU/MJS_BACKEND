package nova.mjs.domain.thingo.broadcast.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class BroadcastException extends BusinessBaseException {
    public BroadcastException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BroadcastException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
