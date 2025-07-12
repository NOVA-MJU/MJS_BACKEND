package nova.mjs.domain.member.exception;

import nova.mjs.util.exception.ErrorCode;

public class MemberNotFoundException extends MemberException{

    public MemberNotFoundException() {super(ErrorCode.MEMBER_NOT_FOUND);}
    public MemberNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    public MemberNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
