package nova.mjs.domain.thingo.weeklyMenu.exception;

import nova.mjs.util.exception.ErrorCode;

public class WeeklyMenuNotFoundException extends WeeklyMenuException{

    public WeeklyMenuNotFoundException() {super(ErrorCode.WEEKLYMENU_NOT_FOUND);}

    public WeeklyMenuNotFoundException(ErrorCode errorCode) {super(errorCode);}
    public WeeklyMenuNotFoundException(String message, ErrorCode errorCode) {super(message, errorCode);}
}
