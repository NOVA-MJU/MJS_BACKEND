package nova.mjs.weather.service;

import nova.mjs.weather.Weather;

/**
 * 날씨 조회 서비스 인터페이스
 * CQRS 패턴의 Query 부분을 담당
 */
public interface WeatherQueryService {
    
    /**
     * 저장된 날씨 데이터 조회
     */
    Weather getStoredWeather();
}