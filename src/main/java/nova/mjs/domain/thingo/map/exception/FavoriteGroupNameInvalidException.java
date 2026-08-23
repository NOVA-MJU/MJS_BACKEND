package nova.mjs.domain.thingo.map.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

/**
 * 그룹명이 형식(1~12자, 공백 포함)에 맞지 않을 때 발생.
 */
public class FavoriteGroupNameInvalidException extends BusinessBaseException {

    public FavoriteGroupNameInvalidException() {
        super(ErrorCode.FAVORITE_GROUP_NAME_INVALID);
    }

    public FavoriteGroupNameInvalidException(String message) {
        super(message, ErrorCode.FAVORITE_GROUP_NAME_INVALID);
    }
}
