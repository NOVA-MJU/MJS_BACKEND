package nova.mjs.weather.weather;

import nova.mjs.weather.Weather;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class WeatherTest {

    @Test
    @DisplayName("Weather 객체 생성 및 필드 확인 테스트")
    void createWeatherAndVerifyFields() {
        // given
        LocalDateTime now = LocalDateTime.now();

        Weather weather = Weather.builder()
                .location("Busan")
                .temperature(18.2)
                .feelsLike(17.5)
                .humidity(70)
                .weatherMain("Clouds")
                .weatherDescription("overcast clouds")
                .weatherIcon("04d")
                .minTemperature(16.0)
                .maxTemperature(20.0)
                .pm10(45.0)
                .pm2_5(30.0)
                .pm10Category("Moderate")
                .pm2_5Category("Unhealthy")
                .timestamp(now)
                .build();

        // then
        assertThat(weather.getLocation()).isEqualTo("Busan");
        assertThat(weather.getTemperature()).isEqualTo(18.2);
        assertThat(weather.getFeelsLike()).isEqualTo(17.5);
        assertThat(weather.getHumidity()).isEqualTo(70);
        assertThat(weather.getWeatherMain()).isEqualTo("Clouds");
        assertThat(weather.getWeatherDescription()).isEqualTo("overcast clouds");
        assertThat(weather.getWeatherIcon()).isEqualTo("04d");
        assertThat(weather.getMinTemperature()).isEqualTo(16.0);
        assertThat(weather.getMaxTemperature()).isEqualTo(20.0);
        assertThat(weather.getPm10()).isEqualTo(45.0);
        assertThat(weather.getPm2_5()).isEqualTo(30.0);
        assertThat(weather.getPm10Category()).isEqualTo("Moderate");
        assertThat(weather.getPm2_5Category()).isEqualTo("Unhealthy");
        assertThat(weather.getTimestamp()).isEqualTo(now);
    }
}
