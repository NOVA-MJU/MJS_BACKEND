package nova.mjs.domain.mentorship.mentor.exception;

import nova.mjs.domain.thingo.member.exception.MemberException;
import nova.mjs.util.exception.ErrorCode;

public class MentorNotFoundException extends MentorException {

    public MentorNotFoundException() {super(ErrorCode.MEMBER_NOT_FOUND);}
    public MentorNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    public MentorNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
