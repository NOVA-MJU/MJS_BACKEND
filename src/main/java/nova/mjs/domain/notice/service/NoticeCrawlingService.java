package nova.mjs.domain.notice.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.notice.dto.NoticeResponseDto;
import nova.mjs.domain.notice.entity.Notice;
import nova.mjs.domain.notice.exception.NoticeCrawlingException;
import nova.mjs.domain.notice.repository.NoticeRepository;
import nova.mjs.util.exception.ErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 조회
public class NoticeCrawlingService {

    private final NoticeRepository noticeRepository;

    // 공지 URL 매핑
    private static final String BASE_URL = "https://www.mju.ac.kr/";
    private static final String SUBVIEW_BASE = "https://www.mju.ac.kr/mjukr/255/subview.do?enc=";

    // 카테고리
    private static final Map<String, String> NOTICE_URLS = Map.of(
            "general", "mjukr/255/subview.do",     // 일반 공지
            "academic", "mjukr/257/subview.do",    // 학사 공지
            "scholarship", "mjukr/259/subview.do", // 장학/학자금 공지
            "career", "mjukr/260/subview.do",      // 진로/취업/창업 공지
            "activity", "mjukr/5364/subview.do",   // 학생활동 공지
            "rule", "mjukr/4450/subview.do",       // 학칙개정 공지
            "법학과", "col/1299/subview.do"        // 법학과 공지
    );

    /**
     * 모든 카테고리 공지 크롤링
     * - 크롤링은 트랜잭션 밖
     * - 저장만 트랜잭션
     */
    @Transactional
    public List<NoticeResponseDto> fetchAllNotices() {
        List<NoticeResponseDto> result = new ArrayList<>();
        for (String type : NOTICE_URLS.keySet()) {
            result.addAll(fetchNotices(type));
        }
        return result;
    }

    /**
     * 특정 카테고리 공지 크롤링
     */
    public List<NoticeResponseDto> fetchNotices(String type) {

        // (1) URL 유효성 체크
        String url = NOTICE_URLS.get(type);
        if (url == null) {
            throw new IllegalArgumentException("잘못된 공지 타입입니다: " + type);
        }

        int cutoffYear = LocalDate.now().getYear() - 2;
        int page = 1;
        boolean stop = false;

        List<Notice> noticesToSave = new ArrayList<>();
        List<NoticeResponseDto> response = new ArrayList<>();

        // (2) 페이지 단위 크롤링
        while (!stop) {
            String fullUrl = BASE_URL + url + "?page=" + page;
            log.info("[MJS] Requesting URL: {}", fullUrl);

            try {
                Document doc = Jsoup.connect(fullUrl).get();
                Elements rows = doc.select("tr:not(.headline):not(._artclOdd)");

                if (rows.isEmpty()) break;

                for (Element row : rows) {

                    // (3) 목록 페이지 데이터 추출
                    String rawDate = row.select("._artclTdRdate").text();
                    String rawTitle = row.select(".artclLinkView strong").text();
                    String rawLink = row.select(".artclLinkView").attr("href");

                    LocalDateTime date = normalizeDate(rawDate);
                    String title = normalizeTitle(rawTitle);
                    String category = normalizeCategory(type);

                    if (date == null || title.isEmpty()) continue;

                    // (4) 오래된 공지면 중단
                    if (date.getYear() <= cutoffYear) {
                        stop = true;
                        break;
                    }

                    // (5) 중복 공지 체크
                    boolean exists =
                            noticeRepository.existsByDateAndCategoryAndTitle(date, category, title);
                    if (exists) {
                        stop = true;
                        break;
                    }

                    // (6) 상세 페이지 링크 생성
                    String encodedUrl = encodeArtclViewToEnc(rawLink);
                    String finalUrl = SUBVIEW_BASE + encodedUrl;

                    // (7) 상세 페이지 content 크롤링
                    String content = crawlNoticeContent(finalUrl);

                    // (8) 엔티티 생성 (이미 완성된 상태)
                    Notice notice = Notice.createNotice(title, content, date, category, finalUrl);

                    noticesToSave.add(notice);
                    response.add(NoticeResponseDto.noticeEntity(notice));
                }

                page++;

            } catch (Exception e) {
                log.error("[MJS] {} 타입 공지 크롤링 실패", type, e);
                throw new NoticeCrawlingException("공지 크롤링 실패", ErrorCode.SCHEDULER_TASK_FAILED);
            }
        }

        // (9) 저장은 마지막에 한 번만 (짧은 트랜잭션)
        saveNotices(noticesToSave);

        log.info("[MJS] {} 타입 공지 크롤링 완료. 총 {}건", type, response.size());
        return response;
    }

    /**
     * DB 저장 전용 트랜잭션
     */
    @Transactional
    protected void saveNotices(List<Notice> notices) {
        if (!notices.isEmpty()) {
            noticeRepository.saveAll(notices);
        }
    }

    /**
     * 공지 본문 크롤링 + 정제
     */
    private String crawlNoticeContent(String link) {
        try {
            Document doc = Jsoup.connect(link).get();
            Element contentEl = doc.selectFirst("div.artclView");
            if (contentEl == null) return null;
            return cleanNoticeContent(contentEl);
        } catch (Exception e) {
            log.warn("[MJS] content crawling failed. link={}", link, e);
            return null;
        }
    }

    /**
     * 본문 HTML 정제
     */
    private String cleanNoticeContent(Element contentEl) {

        // 1. style / class / id 제거
        contentEl.select("*").forEach(el -> {
            el.removeAttr("style");
            el.removeAttr("class");
            el.removeAttr("id");
        });

        // 2. 편집기 잔재 제거
        contentEl.select("div[data-hjsonver], div#hwpEditorBoardContent").remove();

        // 3. 빈 span 제거
        contentEl.select("span").forEach(span -> {
            if (span.text().trim().isEmpty() && span.children().isEmpty()) {
                span.remove();
            }
        });

        // 4. 의미 없는 p 제거
        contentEl.select("p").forEach(p -> {
            boolean hasText = !p.text().trim().isEmpty();
            boolean hasMedia = !p.select("img, a").isEmpty();
            if (!hasText && !hasMedia) p.remove();
        });

        return contentEl.html().trim();
    }

    /* ===================== 문자열 정규화 ===================== */

    private LocalDateTime normalizeDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return null;
        try {
            return LocalDate.parse(
                    rawDate.trim().replaceAll("\\s+", "").replaceAll("\\.\\s*", "-")
            ).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeTitle(String rawTitle) {
        return rawTitle == null ? "" : rawTitle.trim().replaceAll("\\s+", " ");
    }

    private String normalizeCategory(String rawCategory) {
        return rawCategory == null ? "" : rawCategory.trim().toLowerCase();
    }

    private String encodeArtclViewToEnc(String rawLink) {
        String path = rawLink.split("\\?")[0];
        if (!path.startsWith("/")) path = "/" + path;

        String query =
                "?page=1&srchColumn=&srchWrd=&bbsClSeq=&bbsOpenWrdSeq=" +
                        "&rgsBgndeStr=&rgsEnddeStr=&isViewMine=false&isView=true&password=";

        String full = "fnct1|@@|" + path + query;
        return URLEncoder.encode(
                Base64.getEncoder().encodeToString(full.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8
        );
    }
}
