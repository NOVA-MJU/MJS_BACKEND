package nova.weather;

import nova.mjs.weather.Weather;
import nova.mjs.weather.WeatherController;
import nova.mjs.weather.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
@ContextConfiguration(classes = { WeatherController.class, WeatherService.class }) // 또는 명시적으로 빈 설정
@AutoConfigureMockMvc(addFilters = false)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    @DisplayName("GET /api/v1/weather 요청 시 Weather 응답 반환")
    void getLatestWeather_ShouldReturnWeatherObject() throws Exception {
        // given
        Weather mockWeather = Weather.builder()
                .location("Seoul")
                .temperature(22.5)
                .feelsLike(21.0)
                .humidity(55)
                .weatherMain("Clear")
                .weatherDescription("clear sky")
                .weatherIcon("01d")
                .minTemperature(20.0)
                .maxTemperature(25.0)
                .pm10(40.0)
                .pm2_5(20.0)
                .pm10Category("Moderate")
                .pm2_5Category("Good")
                .timestamp(LocalDateTime.now())
                .build();

        given(weatherService.getStoredWeather()).willReturn(mockWeather);

        // when & then
        mockMvc.perform(get("/api/v1/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Seoul"))
                .andExpect(jsonPath("$.temperature").value(22.5))
                .andExpect(jsonPath("$.weatherMain").value("Clear"));
    }
}
