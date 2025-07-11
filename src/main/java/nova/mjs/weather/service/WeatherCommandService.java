package nova.mjs.weather.service;

/**
 * 날씨 변경 서비스 인터페이스
 * CQRS 패턴의 Command 부분을 담당
 */
public interface WeatherCommandService {
    
    /**
     * 날씨 데이터 크롤링 및 저장
     */
    void fetchAndStoreWeatherData();
}