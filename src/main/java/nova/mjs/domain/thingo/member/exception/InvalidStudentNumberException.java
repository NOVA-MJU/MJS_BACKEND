package nova.mjs.domain.thingo.member.exception;

import nova.mjs.util.exception.ErrorCode;

public class InvalidStudentNumberException extends MemberException{

    public InvalidStudentNumberException() {super(ErrorCode.INVALID_STUDENT_NUMBER_EXCEPTION);}
    public InvalidStudentNumberException(ErrorCode errorCode) {
        super(errorCode);
    }
    public InvalidStudentNumberException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
