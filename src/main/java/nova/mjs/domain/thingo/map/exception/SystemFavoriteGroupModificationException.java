package nova.mjs.domain.thingo.map.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

/**
 * 시스템 기본 그룹('내 장소'·'버스')을 수정/삭제하려 할 때 발생.
 */
public class SystemFavoriteGroupModificationException extends BusinessBaseException {

    public SystemFavoriteGroupModificationException() {
        super(ErrorCode.FAVORITE_GROUP_SYSTEM_MODIFY_NOT_ALLOWED);
    }

    public SystemFavoriteGroupModificationException(String message) {
        super(message, ErrorCode.FAVORITE_GROUP_SYSTEM_MODIFY_NOT_ALLOWED);
    }
}
