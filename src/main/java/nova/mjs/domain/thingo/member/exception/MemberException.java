package nova.mjs.domain.thingo.member.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class MemberException extends BusinessBaseException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }
    public MemberException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}