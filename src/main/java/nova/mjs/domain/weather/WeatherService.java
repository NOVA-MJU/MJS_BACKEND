package nova.mjs.domain.weather;

import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.weather.exception.WeatherAPICallException;
import nova.mjs.domain.weather.exception.WeatherJsonParseException;
import nova.mjs.domain.weather.exception.WeatherNotFoundException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class WeatherService {

    private final WebClient webClient;
    private final WeatherRepository weatherRepository;

    @Value("${weather.apikey}")
    private String apiKey;

    private static final String WEATHER_API_URL = "/data/3.0/onecall"
            + "?lat=37.5687&lon=126.9221&exclude=hourly,minutely"
            + "&appid=%s&units=metric&lang=kr";

    private static final String AIR_POLLUTION_API_URL = "/data/2.5/air_pollution"
            + "?lat=37.5687&lon=126.9221&appid=%s";

    private static final String ICON_BASE_URL = "https://openweathermap.org/img/wn/%s@2x.png";

    public WeatherService(@Qualifier("openWeatherMapClient") WebClient webClient, WeatherRepository weatherRepository) {
        this.webClient = webClient;
        this.weatherRepository = weatherRepository;
    }

    @Transactional
    public void fetchAndStoreWeatherData() {
        String weatherUrl = String.format(WEATHER_API_URL, apiKey);
        String airPollutionUrl = String.format(AIR_POLLUTION_API_URL, apiKey);

        webClient.get()
                .uri(weatherUrl)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(weatherResponse -> {
                    log.info("Weather API Response: {}", weatherResponse);
                    return webClient.get().uri(airPollutionUrl).retrieve().bodyToMono(String.class)
                            .map(airPollutionResponse -> {
                                log.info("Air Pollution API Response: {}", airPollutionResponse);
                                return parseAndSaveWeatherData(weatherResponse, airPollutionResponse);
                            });
                })
                .onErrorResume(e -> {
                    log.error("API 호출 오류 발생 {}", e.getMessage(), e);
                    return Mono.error(new WeatherAPICallException());
                })
                .subscribe();
    }

    private Weather parseAndSaveWeatherData(String weatherResponse, String airPollutionResponse) {
        try {
            log.info("데이터 파싱 시작...");
            JSONObject weatherJson = new JSONObject(weatherResponse);
            JSONObject airPollutionJson = new JSONObject(airPollutionResponse);

            JSONObject current = weatherJson.getJSONObject("current");
            JSONObject daily = weatherJson.getJSONArray("daily").getJSONObject(0);
            JSONObject weather = current.getJSONArray("weather").getJSONObject(0);

            double temp = current.getDouble("temp");
            double feelsLike = current.getDouble("feels_like");
            int humidity = current.getInt("humidity");
            String weatherMain = weather.getString("main");
            String weatherDescription = weather.getString("description");
            String weatherIconCode = weather.getString("icon");
            String weatherIconUrl = String.format(ICON_BASE_URL, weatherIconCode);
            double minTemp = daily.getJSONObject("temp").getDouble("min");
            double maxTemp = daily.getJSONObject("temp").getDouble("max");

            JSONObject airComponents = airPollutionJson.getJSONArray("list").getJSONObject(0).getJSONObject("components");
            double pm10 = airComponents.getDouble("pm10");
            double pm2_5 = airComponents.getDouble("pm2_5");

            String pm10Category = categorizePm10(pm10);
            String pm2_5Category = categorizePm2_5(pm2_5);

            Weather weatherEntity = Weather.builder()
                    .location("서울시 남가좌동")
                    .temperature(temp)
                    .feelsLike(feelsLike)
                    .humidity(humidity)
                    .weatherMain(weatherMain)
                    .weatherDescription(weatherDescription)
                    .weatherIcon(weatherIconUrl)
                    .minTemperature(minTemp)
                    .maxTemperature(maxTemp)
                    .pm10(pm10)
                    .pm2_5(pm2_5)
                    .pm10Category(pm10Category)
                    .pm2_5Category(pm2_5Category)
                    .timestamp(LocalDateTime.now())
                    .build();

            weatherRepository.deleteAll();
            weatherRepository.save(weatherEntity);
            log.info("날씨 데이터 저장 완료");

            return weatherEntity;
        } catch (Exception e) {
            log.error("JSON 데이터 파싱 오류 발생: {}", e.getMessage(), e);
            throw new WeatherJsonParseException();
        }
    }

    private String categorizePm10(double pm10) {
        if (pm10 <= 30) return "좋음";
        if (pm10 <= 80) return "보통";
        if (pm10 <= 150) return "나쁨";
        return "매우 나쁨";
    }

    private String categorizePm2_5(double pm2_5) {
        if (pm2_5 <= 15) return "좋음";
        if (pm2_5 <= 35) return "보통";
        if (pm2_5 <= 75) return "나쁨";
        return "매우 나쁨";
    }


    public Weather getStoredWeather() {
        return weatherRepository.findAll().stream()
                .findFirst()
                .orElseThrow(WeatherNotFoundException::new);
    }
}
