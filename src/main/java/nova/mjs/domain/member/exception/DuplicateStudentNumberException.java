package nova.mjs.domain.member.exception;

import nova.mjs.util.exception.ErrorCode;

public class DuplicateStudentNumberException extends MemberException{

    public DuplicateStudentNumberException() {super(ErrorCode.DUPLICATE_STUDENT_NUMBER_EXCEPTION);}
    public DuplicateStudentNumberException(ErrorCode errorCode) {
        super(errorCode);
    }
    public DuplicateStudentNumberException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
