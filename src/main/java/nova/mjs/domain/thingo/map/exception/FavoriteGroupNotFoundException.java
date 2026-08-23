package nova.mjs.domain.thingo.map.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

/**
 * 요청한 즐겨찾기 그룹을 찾을 수 없을 때 발생.
 */
public class FavoriteGroupNotFoundException extends BusinessBaseException {

    public FavoriteGroupNotFoundException() {
        super(ErrorCode.FAVORITE_GROUP_NOT_FOUND);
    }

    public FavoriteGroupNotFoundException(String message) {
        super(message, ErrorCode.FAVORITE_GROUP_NOT_FOUND);
    }
}
