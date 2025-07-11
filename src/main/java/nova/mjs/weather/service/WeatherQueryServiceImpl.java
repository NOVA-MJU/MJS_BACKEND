package nova.mjs.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.weather.Weather;
import nova.mjs.weather.WeatherRepository;
import nova.mjs.weather.exception.WeatherNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 날씨 조회 서비스 구현체
 * CQRS 패턴의 Query 부분을 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WeatherQueryServiceImpl implements WeatherQueryService {

    private final WeatherRepository weatherRepository;

    @Override
    public Weather getStoredWeather() {
        return weatherRepository.findAll().stream()
                .findFirst()
                .orElseThrow(WeatherNotFoundException::new);
    }
}