package nova.util;

import nova.mjs.MjsApplication;
import nova.mjs.util.scheduler.SchedulerService;
import nova.mjs.news.service.NewsService;
import nova.mjs.weather.WeatherService;
import nova.mjs.weeklyMenu.service.WeeklyMenuService;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.scheduler.exception.SchedulerTaskFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = MjsApplication.class)
class SchedulerServiceTest {

    @Autowired
    SchedulerService schedulerService;

    @MockBean
    NewsService newsService;

    @MockBean
    WeatherService weatherService;

    @MockBean
    WeeklyMenuService weeklyMenuService;

    @Test
    @DisplayName("날씨 스케줄러가 weatherService를 호출하는지 확인")
    void testScheduledFetchWeatherData() {
        schedulerService.scheduledFetchWeatherData();
        sleep(); // CompletableFuture.wait()
        then(weatherService).should(times(1)).fetchAndStoreWeatherData();
    }

    @Test
    @DisplayName("뉴스 스케줄러가 newsService를 호출하는지 확인")
    void testScheduledCrawlNews() {
        schedulerService.scheduledCrawlNews();
        sleep();
        then(newsService).should(times(1)).crawlAndSaveNews(null);
    }

    @Test
    @DisplayName("식단 스케줄러가 weeklyMenuService를 호출하는지 확인")
    void testScheduledCrawlWeeklyMenu() {
        schedulerService.scheduledCrawlWeeklyMenu();
        sleep();
        then(weeklyMenuService).should(times(1)).crawlWeeklyMenu();
    }

    @Test
    @DisplayName("겹치는 시간대에 두 스케줄러가 별도로 동시에 실행되는지 확인")
    void testConcurrentSchedulerExecution() {
        // 스레드 실행 시간 기록용
        List<String> executionLog = Collections.synchronizedList(new ArrayList<>());

        // mock 동작 지정 (일부러 sleep 줘서 병렬성 확인)
        willAnswer(invocation -> {
            executionLog.add("weather-start");
            Thread.sleep(300);
            executionLog.add("weather-end");
            return null;
        }).given(weatherService).fetchAndStoreWeatherData();

        willAnswer(invocation -> {
            executionLog.add("news-start");
            Thread.sleep(300);
            executionLog.add("news-end");
            return null;
        }).given(newsService).crawlAndSaveNews(null);

        // 두 개를 거의 동시에 실행
        schedulerService.scheduledFetchWeatherData();
        schedulerService.scheduledCrawlNews();
        sleep(700); // CompletableFuture 실행 대기

        // 실행 로그 확인
        System.out.println("실행 로그: " + executionLog);

        // 실행 확인
        assertThat(executionLog).contains("weather-start", "news-start", "weather-end", "news-end");

        // 동시에 시작했는지 확인 (순차 실행이면 start1 → end1 → start2 → end2 식으로 됨)
        int weatherStartIndex = executionLog.indexOf("weather-start");
        int newsStartIndex = executionLog.indexOf("news-start");

        assertThat(Math.abs(weatherStartIndex - newsStartIndex)).isLessThanOrEqualTo(1);
    }


    private void sleep() {
        try {
            Thread.sleep(500); // CompletableFuture가 실행될 시간을 줌
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
