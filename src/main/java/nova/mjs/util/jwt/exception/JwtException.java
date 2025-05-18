package nova.mjs.util.jwt.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class JwtException extends BusinessBaseException {

    public JwtException(ErrorCode errorCode) {
        super(errorCode);
    }

    public JwtException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
