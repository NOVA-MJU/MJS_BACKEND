package nova.mjs.notice.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeCrawlingService {

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

    public List<NoticeResponseDto> fetchNotices(String type) {
        List<NoticeResponseDto> notices = new ArrayList<>();

        // URL을 가져옴. 존재하지 않으면 예외 발생.
        String url = NOTICE_URLS.get(type);
        if (url == null) {
            throw new BusinessBaseException("잘못된 공지 타입입니다: " + type, ErrorCode.INVALID_PARAM_REQUEST);
        }

        int cutoffYear = LocalDate.now().getYear() - 2;
        LocalDate currentDate = LocalDate.now();
        String currentDateString = currentDate.toString();

        try {
            int page = 1;
            boolean stop = false;

            while (!stop) {
                String fullUrl = BASE_URL + url + "?page=" + page;
                System.out.println("Requesting URL: " + fullUrl); // 디버깅용 URL 출력

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

                    notices.add(new NoticeResponseDto(title, dateText, type, currentDateString, link));
                }

                if (rows.isEmpty() || stop) {
                    break;
                } else {
                    page++;
                }
            }
        } catch (Exception e) {
            throw new BusinessBaseException("공지사항 크롤링 중 오류 발생: " + e.getMessage(), ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return notices;
    }
}
