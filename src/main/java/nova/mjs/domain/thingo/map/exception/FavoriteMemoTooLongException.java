package nova.mjs.domain.thingo.map.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

/**
 * 메모가 최대 길이(30자, 공백 포함)를 초과할 때 발생.
 */
public class FavoriteMemoTooLongException extends BusinessBaseException {

    public FavoriteMemoTooLongException() {
        super(ErrorCode.FAVORITE_MEMO_TOO_LONG);
    }

    public FavoriteMemoTooLongException(String message) {
        super(message, ErrorCode.FAVORITE_MEMO_TOO_LONG);
    }
}
