package nova.mjs.weather.exception;

import nova.mjs.util.exception.ErrorCode;

public class WeatherAPICallException extends WeatherException {
    public WeatherAPICallException() {
        super(ErrorCode.API_CALL_FAILED);
    }
}
