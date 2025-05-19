//package nova.weather;
//
//import nova.mjs.MjsApplication;
//import nova.mjs.weather.Weather;
//import nova.mjs.weather.WeatherRepository;
//import nova.mjs.weather.WeatherService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(classes = MjsApplication.class)
//public class WeatherServiceApiTest {
//
//    @Autowired
//    private WeatherService weatherService;
//
//    @Autowired
//    private WeatherRepository weatherRepository;
//
//    @Test
//    @DisplayName("실제 API 호출로 날씨 데이터 저장 확인")
//    void fetchAndStoreWeatherData_realApiCall() throws InterruptedException {
//        // given
//        weatherRepository.deleteAll();
//
//        // when
//        weatherService.fetchAndStoreWeatherData();
//
//        // then
//        Thread.sleep(2000);
//
//        List<Weather> result = weatherRepository.findAll();
//        assertThat(result).hasSize(1);
//
//        Weather weather = result.get(0);
//        System.out.println("🌤 실제 저장 결과: " + weather.getWeatherMain() + " / " + weather.getTemperature() + "도");
//
//        assertThat(weather.getWeatherMain()).isNotBlank();
//        assertThat(weather.getTemperature()).isGreaterThan(-50);
//    }
//}
