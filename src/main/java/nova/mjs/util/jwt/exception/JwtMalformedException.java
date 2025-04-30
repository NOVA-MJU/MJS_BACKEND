package nova.mjs.util.jwt.exception;

import nova.mjs.util.exception.ErrorCode;

public class JwtMalformedException extends JwtException{

    public JwtMalformedException() {
        super(ErrorCode.TOKEN_MALFORMED);
    }

    public JwtMalformedException(String message) {
        super(message, ErrorCode.TOKEN_MALFORMED);
    }
}
