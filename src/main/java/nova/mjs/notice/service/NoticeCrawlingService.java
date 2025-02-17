package nova.mjs.notice.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.entity.Notice;

import nova.mjs.notice.repository.NoticeRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static nova.mjs.notice.dto.NoticeResponseDto.noticeEntity;
import static nova.mjs.notice.entity.Notice.createNotice;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeCrawlingService {

    private final NoticeRepository noticeRepository;
    // 공지 URL 매핑
    private static final String BASE_URL = "https://www.mju.ac.kr/";
    private static final Map<String, String> NOTICE_URLS = Map.of(
            "general", "mjukr/255/subview.do", // 일반 공지
            "academic", "mjukr/257/subview.do", // 학사 공지
            "scholarship", "mjukr/259/subview.do", // 장학/학자금 공지
            "career", "mjukr/260/subview.do", // 진로/취업/창업 공지
            "activity", "mjukr/5364/subview.do", // 학생활동 공지
            "rule", "mjukr/4450/subview.do"  // 학칙개정 공지
    );

    @Transactional
    public List<NoticeResponseDto> fetchNotices(String type) {
        List<NoticeResponseDto> notices = new ArrayList<>();
        List<Notice> noticeEntities = new ArrayList<>(); // Batch Insert를 위한 리스트


        // URL을 가져옴. 존재하지 않으면 예외 발생.
        String url = NOTICE_URLS.get(type);
        if (url == null) {
            throw new IllegalArgumentException("잘못된 공지 타입입니다: " + type);
        }

        int cutoffYear = LocalDate.now().getYear() - 2;
        LocalDate currentDate = LocalDate.now();
        String currentDateString = currentDate.toString();

        try {
            int page = 1;
            boolean stop = false;

            while (!stop) {
                String fullUrl = BASE_URL + url + "?page=" + page;
                log.info("Requesting URL: {}", fullUrl); // SLF4J로 대체

                // Jsoup을 통한 HTTP 요청 및 파싱
                Document doc = Jsoup.connect(fullUrl).get();
                Elements rows = doc.select("tr");

                for (Element row : rows) {
                    String dateText = row.select("._artclTdRdate").text();
                    String title = row.select(".artclLinkView strong").text();
                    String link = row.select(".artclLinkView").attr("href");

                    if (!link.isEmpty()) {
                        link = BASE_URL + link;
                    }

                    if (dateText.isEmpty() || title.isEmpty() || link.isEmpty()) continue;

                    if (dateText.startsWith(String.valueOf(cutoffYear))) {
                        stop = true;
                        break;
                    }

                    Notice notice = createNotice(title, dateText, type, link);
                    noticeEntities.add(notice); // Entity 리스트에 추가
                    notices.add(NoticeResponseDto.noticeEntity(notice)); // Response DTO 리스트에 추가
                }
                noticeRepository.saveAll(noticeEntities);

                if (rows.isEmpty() || stop) {
                    break;
                } else {
                    page++;
                }
            }
            // 반복문이 끝난 후 한 번에 저장
            if (!noticeEntities.isEmpty()) {
                noticeRepository.saveAll(noticeEntities);
                log.info("총 {}개의 공지를 저장했습니다.", noticeEntities.size());
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument error during crawling: {}", e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리
        } catch (Exception e) {
            log.error("Unexpected error during crawling: {}", e.getMessage());
            throw new RuntimeException("크롤링 중 알 수 없는 오류가 발생했습니다.", e); // GlobalExceptionHandler에서 처리
        }

        return notices;
    }
}
