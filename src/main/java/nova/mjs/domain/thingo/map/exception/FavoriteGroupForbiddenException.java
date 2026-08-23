package nova.mjs.domain.thingo.map.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

/**
 * 다른 회원의 즐겨찾기 그룹에 접근하려 할 때 발생.
 */
public class FavoriteGroupForbiddenException extends BusinessBaseException {

    public FavoriteGroupForbiddenException() {
        super(ErrorCode.FAVORITE_GROUP_FORBIDDEN);
    }

    public FavoriteGroupForbiddenException(String message) {
        super(message, ErrorCode.FAVORITE_GROUP_FORBIDDEN);
    }
}
