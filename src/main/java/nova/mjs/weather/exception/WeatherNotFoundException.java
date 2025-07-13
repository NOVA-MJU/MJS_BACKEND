package nova.mjs.weather.exception;

import nova.mjs.util.exception.ErrorCode;

public class WeatherNotFoundException extends WeatherException {
    public WeatherNotFoundException() {super(ErrorCode.NO_DATA_FOUND); }

}
