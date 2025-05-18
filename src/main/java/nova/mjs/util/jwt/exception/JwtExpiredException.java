package nova.mjs.util.jwt.exception;

import nova.mjs.util.exception.ErrorCode;

public class JwtExpiredException extends JwtException {

    public JwtExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }

    public JwtExpiredException(String message) {
        super(message, ErrorCode.TOKEN_EXPIRED);
    }
}
