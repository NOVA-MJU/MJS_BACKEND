package nova.mjs.domain.weather.exception;

import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

public class WeatherException extends BusinessBaseException {
    public WeatherException(ErrorCode errorCode) {
        super(errorCode);
    }
    public WeatherException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}