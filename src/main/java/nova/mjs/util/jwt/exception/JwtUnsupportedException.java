package nova.mjs.util.jwt.exception;

import nova.mjs.util.exception.ErrorCode;

public class JwtUnsupportedException extends JwtException{

    public JwtUnsupportedException() {
        super(ErrorCode.TOKEN_UNSUPPORTED);
    }

    public JwtUnsupportedException(String message) {
        super(message, ErrorCode.TOKEN_UNSUPPORTED);
    }
}
