package nova.mjs.util.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.news.service.NewsService;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.scheduler.exception.SchedulerCronInvalidException;
import nova.mjs.util.scheduler.exception.SchedulerTaskFailedException;
import nova.mjs.util.scheduler.exception.SchedulerUnknownException;
import nova.mjs.weather.WeatherService;
import nova.mjs.weeklyMenu.service.WeeklyMenuService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

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
        CompletableFuture.runAsync(() -> {
            try {
                weatherService.fetchAndStoreWeatherData();
                log.info("날씨 데이터 업데이트 완료");
            } catch (IllegalArgumentException e) {
                log.error("잘못된 Cron 표현식 오류 : {}", e.getMessage());
                throw new SchedulerCronInvalidException("잘못된 Cron 표현식", ErrorCode.SCHEDULER_CRON_INVALID);
            } catch (Exception e) {
                log.error("날씨 크롤링 중 오류 발생 : {}", e.getMessage());
                throw new SchedulerTaskFailedException("날씨 데이터 업데이트 실패", ErrorCode.SCHEDULER_TASK_FAILED);
            } catch (Throwable t) {
                log.error("알 수 없는 스케줄러 오류 발생 : {}", t.getMessage(), t);
                throw new SchedulerUnknownException("알 수 없는 스케줄러 오류 발생", ErrorCode.SCHEDULER_UNKNOWN_ERROR);
            }
        });
    }

    //뉴스 크롤링 스케줄링 (매주 월요일 정각마다 실행)
    @Scheduled(cron = "0 0 * * 1 *")
    public void scheduledCrawlNews() {
        log.info("[스케쥴러] 매시간 5분마다 기사 크롤링 실행");
        CompletableFuture.runAsync(() -> {
            try {
                newsService.crawlAndSaveNews(null);
                log.info("기사 데이터 업데이트");
            } catch (IllegalArgumentException e) {
                log.error("잘못된 Cron 표현식 오류 : {}", e.getMessage());
                throw new SchedulerCronInvalidException("잘못된 Cron 표현식입니다.", ErrorCode.SCHEDULER_CRON_INVALID);
            } catch (Exception e) {
                log.error("기사 크롤링 오류 발생 : {}", e.getMessage());
                throw new SchedulerTaskFailedException("기사 크롤링 실패", ErrorCode.SCHEDULER_TASK_FAILED);
            } catch (Throwable t) {
                log.error("알 수 없는 스케줄러 오류 발생 : {}", t.getMessage(), t);
                throw new SchedulerUnknownException("알 수 없는 스케줄러 오류 발생", ErrorCode.SCHEDULER_UNKNOWN_ERROR);
            }
        });
    }

    //식단 데이터 크롤링 스케줄링 (매주 토, 일, 월 19:00 실행)
    @Scheduled(cron = "0 0 19 * * 6") // 매주 토요일 19:00 실행
    @Scheduled(cron = "0 0 19 * * 7") // 매주 일요일 19:00 실행
    @Scheduled(cron = "0 0 19 * * 1") // 매주 월요일 19:00 실행
    public void scheduledCrawlWeeklyMenu() {
        log.info("[스케쥴러] 매주 토, 일, 월 19시에 식단 크롤링 실행");
        CompletableFuture.runAsync(() -> {
            try {
                weeklyMenuService.crawlWeeklyMenu();
                log.info("식단 데이터 업데이트 완료");
            } catch (IllegalArgumentException e) {
                log.error("잘못된 Cron 표현식 오류: {}", e.getMessage());
                throw new SchedulerCronInvalidException("잘못된 Cron 표현식입니다.", ErrorCode.SCHEDULER_CRON_INVALID);
            } catch (Exception e) {
                log.error("식단 크롤링 중 오류 발생 : {}", e.getMessage());
                throw new SchedulerTaskFailedException("식단 데이터 크롤링 실패", ErrorCode.SCHEDULER_TASK_FAILED);
            } catch (Throwable t) {
                log.error("알 수 없는 스케줄러 오류 발생 : {}", t.getMessage(), t);
                throw new SchedulerUnknownException("알 수 없는 스케줄러 오류 발생", ErrorCode.SCHEDULER_UNKNOWN_ERROR);
            }
        });
    }
}
