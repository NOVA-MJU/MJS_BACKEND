package nova.mjs.domain.calendar.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class CalendarException extends BusinessBaseException {

    public CalendarException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CalendarException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
