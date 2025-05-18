package nova.mjs.util.jwt.exception;

import nova.mjs.util.exception.ErrorCode;

public class RefreshTokenParseFailedException extends JwtException{

    public RefreshTokenParseFailedException() {
        super(ErrorCode.REFRESH_TOKEN_PARSE_FAILED);
    }

    public RefreshTokenParseFailedException(String message) {
        super(message, ErrorCode.REFRESH_TOKEN_PARSE_FAILED);
    }
}
