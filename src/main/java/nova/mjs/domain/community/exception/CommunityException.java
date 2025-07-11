package nova.mjs.domain.community.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class CommunityException extends BusinessBaseException {

    public CommunityException(ErrorCode errorCode) {
        super(errorCode);
    }
    public CommunityException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}