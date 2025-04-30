package nova.mjs.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeScheduler {
    private final NoticeCrawlingService noticeCrawlingService;

    // 보통 출근-퇴근 중간에 틈틈히 올리시니까
    // 오전 오후 1시간 ~ 1시간 반 쯤 격차 두고 크롤링
    // 퇴근시간 이후는 크롤링 하지 않고 혹시 전날 저녁에 올라온 게 있을 수 있으니 다음날 8시에 크롤링
    @Scheduled(cron = "0 0 8 * * *")   // 매일 08:00
    @Scheduled(cron = "0 30 9 * * *")  // 매일 09:30
    @Scheduled(cron = "0 30 10 * * *") // 매일 10:30
    @Scheduled(cron = "0 0 12 * * *")  // 매일 12:00
    @Scheduled(cron = "0 30 13 * * *") // 매일 13:30
    @Scheduled(cron = "0 00 15 * * *") // 매일 15:00
    @Scheduled(cron = "0 30 16 * * *") // 매일 16:30
    @Scheduled(cron = "0 0 18 * * *")  // 매일 18:00
    //@Scheduled(cron = "0 35 16 * * *")  // TEST
    public void crawlAllNotices() {
        log.info("[MJS] Scheduled crawling started.");
        List<String> noticeTypes = List.of("general", "academic", "scholarship", "career", "activity", "rule");
        for (String type : noticeTypes) {
            noticeCrawlingService.fetchNotices(type);
        }
    }
}
