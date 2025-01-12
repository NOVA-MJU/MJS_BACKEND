package nova.mjs.notice.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import nova.mjs.notice.dto.NoticeResponseDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    // 공지 URL 매핑
    private static final String BASE_URL = "https://www.mju.ac.kr/";
    private static final String[] NOTICE_URLS = {
            "mjukr/255/subview.do", // 일반 공지
            "mjukr/257/subview.do", // 학사 공지
            "mjukr/259/subview.do", // 장학/학자금 공지
            "mjukr/260/subview.do", // 진로/취업/창업 공지
            "mjukr/5364/subview.do", // 학생활동 공지
            "mjukr/4450/subview.do"  // 학칙개정 공지
    };

    public List<NoticeResponseDto> fetchNotices(String type) {
        List<NoticeResponseDto> notices = new ArrayList<>();
        int index = getIndexFromType(type); // type을 URL에 매핑

        if (index == -1) {
            throw new IllegalArgumentException("잘못된 공지 타입입니다. (type: " + type + ")");
        }

        // 현재 시점에서 -2년
        int cutoffYear = LocalDate.now().getYear() - 2;
        LocalDate currentDate = LocalDate.now(); // 현재 날짜
        String currentDateString = currentDate.toString(); // 현재 날짜를 문자열로 변환

        // 공지 타입 문자열 추가
        String noticeType = getTypeName(type); // 타입 이름 (예: "일반공지", "학사공지")
        boolean stop = false;

        try {
            String url = BASE_URL + NOTICE_URLS[index];
            int page = 1;

            while (!stop) { // stop 변수를 조건으로 사용
                // 페이지 URL
                Document doc = Jsoup.connect(url + "?page=" + page).get();

                // 공지사항 리스트 가져오기
                Elements rows = doc.select("tr"); // 공지사항 테이블의 행 선택

                for (Element row : rows) {
                    String dateText = row.select("._artclTdRdate").text(); // 날짜
                    String title = row.select(".artclLinkView strong").text(); // 제목
                    String link = row.select(".artclLinkView").attr("href"); // 링크 (상대 URL)

                    // 절대 URL로 변환
                    if (!link.isEmpty()) {
                        link = BASE_URL + link;
                    }

                    // 유효성 검사
                    if (dateText.isEmpty() || title.isEmpty() || link.isEmpty()) continue;

                    // 날짜가 기준 연도(cutoffYear) 이전이면 종료
                    if (dateText.startsWith(String.valueOf(cutoffYear))) {
                        stop = true; // stop을 true로 설정
                        break; // 현재 for 반복문 종료
                    }

                    // NoticeResponseDto 생성 및 추가
                    notices.add(new NoticeResponseDto(title, dateText, noticeType, currentDateString, link));
                }

                if (!rows.isEmpty() && !stop) {
                    page++; // 다음 페이지로 이동
                } else {
                    break; // 더 이상 데이터가 없거나 stop이 true일 경우 while 종료
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return notices;
    }

    // 공지 타입을 인덱스로 매핑
    private int getIndexFromType(String type) {
        switch (type) {
            case "general": return 0;
            case "academic": return 1;
            case "scholarship": return 2;
            case "career": return 3;
            case "activity": return 4;
            case "rule": return 5;
            default: return -1;
        }
    }

    private String getTypeName(String type) {
        switch (type) {
            case "general": return "일반";
            case "academic": return "학사";
            case "scholarship": return "장학/학자금";
            case "career": return "진로/취업/창업";
            case "activity": return "학생활동";
            case "rule": return "학칙개정";
            default: return "알수없음";
        }
    }
}
