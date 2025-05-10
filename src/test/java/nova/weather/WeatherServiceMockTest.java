package nova.weather;

import nova.mjs.MjsApplication;
import nova.mjs.weather.Weather;
import nova.mjs.weather.WeatherRepository;
import nova.mjs.weather.WeatherService;
import nova.mjs.weather.exception.WeatherAPICallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@SpringBootTest(classes = MjsApplication.class)
class WeatherServiceMockTest {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherRepository weatherRepository;

    @MockBean(name = "openWeatherMapClient")
    private WebClient mockWebClient;

    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        // 모든 이전 데이터 제거
        weatherRepository.deleteAll();

        // WebClient 체이닝 mocking 준비
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        given(mockWebClient.get()).willReturn(requestHeadersUriSpec);
        given(requestHeadersUriSpec.uri(anyString())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    }

    @Test
    @DisplayName("날씨 API와 공기질 API 응답을 받아 저장 테스트")
    void fetchAndStoreWeatherData_success() throws InterruptedException {
        // given
        String weatherJson = """
            {
              "current": {
                "temp": 22.5,
                "feels_like": 21.0,
                "humidity": 60,
                "weather": [{ "main": "Clear", "description": "clear sky", "icon": "01d" }]
              },
              "daily": [
                { "temp": { "min": 18.0, "max": 25.0 } }
              ]
            }
            """;

        String airJson = """
            {
              "list": [
                {
                  "components": {
                    "pm10": 35.0,
                    "pm2_5": 20.0
                  }
                }
              ]
            }
            """;

        // 응답 흐름 mocking
        given(responseSpec.bodyToMono(String.class))
                .willReturn(Mono.just(weatherJson))  // 첫 번째 호출
                .willReturn(Mono.just(airJson));     // 두 번째 호출

        // when
        weatherService.fetchAndStoreWeatherData();

        // then (비동기 동작 대기)
        Thread.sleep(1000);

        List<Weather> all = weatherRepository.findAll();
        assertThat(all).hasSize(1);

        Weather saved = all.get(0);
        assertThat(saved.getTemperature()).isEqualTo(22.5);
        assertThat(saved.getHumidity()).isEqualTo(60);
        assertThat(saved.getPm10()).isEqualTo(35.0);
        assertThat(saved.getPm10Category()).isEqualTo("보통");
        assertThat(saved.getWeatherMain()).isEqualTo("Clear");
    }
}
