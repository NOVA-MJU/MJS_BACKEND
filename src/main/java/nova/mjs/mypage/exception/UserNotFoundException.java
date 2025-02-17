package nova.mjs.mypage.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class UserNotFoundException extends BusinessBaseException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
