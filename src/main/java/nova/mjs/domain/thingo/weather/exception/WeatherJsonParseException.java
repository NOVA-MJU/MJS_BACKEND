package nova.mjs.domain.thingo.weather.exception;

import nova.mjs.util.exception.ErrorCode;

public class WeatherJsonParseException extends WeatherException {
    public WeatherJsonParseException() {
        super(ErrorCode.JSON_PARSING_FAILED);
    }
}
