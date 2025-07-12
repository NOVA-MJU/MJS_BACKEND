package nova.mjs.domain.member.exception;

import nova.mjs.util.exception.ErrorCode;

public class EmailIsInvalidException extends MemberException{

    public EmailIsInvalidException() {super(ErrorCode.EMAIL_IS_INVALID);}
}
