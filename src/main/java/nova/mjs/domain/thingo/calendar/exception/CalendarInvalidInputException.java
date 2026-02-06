package nova.mjs.domain.thingo.calendar.exception;

import nova.mjs.util.exception.ErrorCode;

public class CalendarInvalidInputException extends CalendarException {
    public CalendarInvalidInputException() {
        super(ErrorCode.INVALID_PARAM_REQUEST);
    }
}
