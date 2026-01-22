package nova.mjs.domain.mentorship.mentor.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class MentorException extends BusinessBaseException {

    public MentorException(ErrorCode errorCode) {
        super(errorCode);
    }
    public MentorException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}