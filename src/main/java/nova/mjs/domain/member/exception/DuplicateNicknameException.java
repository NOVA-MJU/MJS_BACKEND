package nova.mjs.domain.member.exception;

import nova.mjs.util.exception.ErrorCode;

public class DuplicateNicknameException extends MemberException{

    public DuplicateNicknameException() {super(ErrorCode.DUPLICATE_NICKNAME_EXCEPTION);}
}
