package nova.mjs.member.exception;

import nova.mjs.util.exception.ErrorCode;

public class NicknameIsInvalidException extends MemberException{

    public NicknameIsInvalidException() {super(ErrorCode.NICKNAME_IS_INVALID);}
}
