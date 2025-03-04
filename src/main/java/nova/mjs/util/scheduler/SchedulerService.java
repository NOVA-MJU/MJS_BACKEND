package nova.mjs.util.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.news.service.NewsService;
import nova.mjs.weather.WeatherService;
import nova.mjs.weeklyMenu.service.WeeklyMenuService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final NewsService newsService;
    private final WeatherService weatherService;
    private final WeeklyMenuService weeklyMenuService;

    //날씨 데이터 스케줄링 (매 정각 실행)
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledFetchWeatherData() {
        log.info("[스케쥴러] 매시간 정각 날씨 데이터 업데이트 실행");
        weatherService.fetchAndStoreWeatherData();
    }

    //뉴스 크롤링 스케줄링 (매시간 정각 실행)
    @Scheduled(cron = "0 */5 * * * *")
    public void scheduledCrawlNews() {
        log.info("[스케쥴러] 매시간 5분마다 기사 크롤링 실행");
        newsService.crawlAndSaveNews(null);
    }

    //식단 데이터 크롤링 스케줄링 (매주 토, 일, 월 19:00 실행)
    @Scheduled(cron = "0 30 19 * * SAT,SUN,MON")
    public void scheduledCrawlWeeklyMenu() {
        log.info("[스케쥴러] 매주 토, 일, 월 19시 30분에 식단 크롤링 실행");
        weeklyMenuService.crawlWeeklyMenu();
    }
}
