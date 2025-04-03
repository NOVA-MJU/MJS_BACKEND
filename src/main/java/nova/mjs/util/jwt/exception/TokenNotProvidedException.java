package nova.mjs.util.jwt.exception;

import nova.mjs.util.exception.ErrorCode;

public class TokenNotProvidedException extends JwtException{

    public TokenNotProvidedException() {
        super(ErrorCode.TOKEN_NOT_PROVIDED);
    }

    public TokenNotProvidedException(String message) {
        super(message, ErrorCode.TOKEN_NOT_PROVIDED);
    }
}
