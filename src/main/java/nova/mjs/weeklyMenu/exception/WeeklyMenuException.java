package nova.mjs.weeklyMenu.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class WeeklyMenuException extends BusinessBaseException {

    public WeeklyMenuException(ErrorCode errorCode) {super(errorCode);}
    public WeeklyMenuException(String message, ErrorCode errorCode) {super(message, errorCode);}
}
