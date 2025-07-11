package nova.mjs.weather;

import lombok.RequiredArgsConstructor;
import nova.mjs.weather.service.WeatherQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherQueryService weatherQueryService;

    @GetMapping
    public Weather getLatestWeather() {
        return weatherQueryService.getStoredWeather();
    }
}