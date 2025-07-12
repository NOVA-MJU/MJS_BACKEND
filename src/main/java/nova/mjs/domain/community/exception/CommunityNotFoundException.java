package nova.mjs.domain.community.exception;

import nova.mjs.util.exception.ErrorCode;

public class CommunityNotFoundException extends CommunityException{

    public CommunityNotFoundException() {
        super(ErrorCode.COMMUNITY_NOT_FOUND);
    }
    public CommunityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommunityNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}

