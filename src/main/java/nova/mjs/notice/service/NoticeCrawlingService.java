package nova.mjs.notice.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.entity.Notice;

import nova.mjs.notice.exception.NoticeCrawlingException;
import nova.mjs.notice.repository.NoticeRepository;
import nova.mjs.util.exception.ErrorCode;
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

    @Transactional // 크롤링 후 DB 저장
    public List<NoticeResponseDto> fetchNotices(String type) {
        // (1) URL 유효성 체크
        String url = NOTICE_URLS.get(type);
        if (url == null) {
            throw new IllegalArgumentException("잘못된 공지 타입입니다: " + type);
        }

        // (2) 크롤링 로직 준비
        int cutoffYear = LocalDate.now().getYear() - 2;
        int page = 1;
        boolean stop = false;

        List<NoticeResponseDto> notices = new ArrayList<>();
        List<Notice> noticeEntities = new ArrayList<>();

        // (3) 페이지를 돌면서 크롤링
        while (!stop) {
            String fullUrl = BASE_URL + url + "?page=" + page;
            log.info("[MJS] Requesting URL: {}", fullUrl);

            try {
                // Jsoup 파싱
                Document doc = Jsoup.connect(fullUrl).get();
                Elements rows = doc.select("tr:not(.headline):not(._artclOdd)");

                // 만약 가져온 row가 없으면 -> 더 이상 공지가 없다고 판단
                if (rows.isEmpty()) {
                    break;
                }

                // (4) 각 row 파싱
                for (Element row : rows) {
                    // (a) 데이터 추출
                    String rawDate = row.select("._artclTdRdate").text();
                    String rawTitle = row.select(".artclLinkView strong").text();
                    String rawLink = row.select(".artclLinkView").attr("href");

                    // (b) 문자열 전처리 (trim, 공백제거, link 정규화 등)
                    String dateText = normalizeDate(rawDate);
                    String title = normalizeTitle(rawTitle);
                    String link = normalizeLink(rawLink);
                    String category = normalizeCategory(type);

                    // 필수 정보가 하나라도 없으면 무시
                    if (dateText.isEmpty() || title.isEmpty() || link.isEmpty()) {
                        continue;
                    }

                    // (c) cutoffYear 보다 오래된 날짜면 중단
                    //    (ex: dateText가 "2022-03-25" 라고 가정 시, "2022" >= cutoffYear?)
                    if (dateText.startsWith(String.valueOf(cutoffYear))) {
                        log.info("[MJS] {}년도 이전 공지 발견 -> 크롤링 중단", cutoffYear);
                        stop = true;
                        break;
                    }

                    // (d) 이미 DB에 존재하는 공지인지 확인
                    log.info("[MJS] Checking exists: date='{}', category='{}', title='{}'",
                            dateText, category, title);

                    boolean exists = noticeRepository.existsByDateAndCategoryAndTitle(dateText, category, title);
                    if (exists) {
                        log.info("[MJS] 이미 DB에 존재하는 공지 발견 -> 크롤링 중단. date={}, category={}, title={}",
                                dateText, category, title);
                        stop = true;
                        break;
                    }

                    // (e) link 처리
                    if (!link.startsWith("http")) {
                        link = BASE_URL + link;
                    }

                    // (f) 새로운 공지 -> DB에 저장할 리스트에 담기
                    Notice notice = Notice.createNotice(title, dateText, type, link);
                    noticeEntities.add(notice);

                    // (g) 클라이언트 응답용 DTO
                    notices.add(NoticeResponseDto.noticeEntity(notice));
                }

                // (5) 중간 저장: 페이지 크롤링이 끝나면, 수집된 Notice들을 DB에 저장
                //    (중복체크가 끝났으니 중복 저장은 발생X)
                if (!noticeEntities.isEmpty()) {
                    noticeRepository.saveAll(noticeEntities);
                    noticeEntities.clear(); // 다음 페이지를 위해 비움
                }

                // (6) 만약 중단 플래그가 세워졌으면 -> while문 탈출
                if (stop) {
                    break;
                }

                // (7) 다음 페이지
                page++;

            } catch (Exception e) {
                log.error("[MJS] {} 타입 공지 크롤링 중 오류 발생: {}", type, e.getMessage(), e);
                throw new NoticeCrawlingException("공지 크롤링 실패", ErrorCode.SCHEDULER_TASK_FAILED); // ← 예외 던지기
            }

        }

        // (8) 최종 결과 로그
        log.info("[MJS] {}타입 공지 크롤링 완료. 총 {}개의 새 공지를 수집했습니다.", type, notices.size());
        return notices;
    }
    // 날짜 문자열 전처리 - 공백제거, yyyy.mm.dd
    private String normalizeDate(String rawDate) {
        if (rawDate == null) return "";
        // 공백 제거
        String cleaned = rawDate.trim().replaceAll("\\s+", "");
        // 예) 2023. 03. 26 → 2023.03.26
        cleaned = cleaned.replaceAll("\\.\\s+", ".");
        return cleaned;
    }

    // 제목 문자열 전처리 - 공백제거
    private String normalizeTitle(String rawTitle) {
        if (rawTitle == null) return "";
        // 공백 제거
        String cleaned = rawTitle.trim().replaceAll("\\s+", " ");
        return cleaned;
    }

    // 링크 문자열 전처리 - 공백제거, 마지막 슬래쉬 제거
    // 얘 중복 못알아 먹어서 걍 제목으로 변경
    private String normalizeLink(String rawLink) {
        if (rawLink == null) return "";
        // 공백 제거
        String cleaned = rawLink.trim();
        // 필요하면 마지막 슬래시 제거 등 추가 처리
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned;
    }

    // 카테고리 문자열 전처리 - 소문자로 통일
    private String normalizeCategory(String rawCategory) {
        if (rawCategory == null) return "";
        // 소문자로 통일
        return rawCategory.trim().toLowerCase();
    }
}