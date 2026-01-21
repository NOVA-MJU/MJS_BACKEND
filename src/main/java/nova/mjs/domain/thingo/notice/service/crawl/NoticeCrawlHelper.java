package nova.mjs.domain.thingo.notice.service.crawl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 공지 크롤링 기술 로직 전담 Helper
 * - 목록 페이지 크롤링
 * - 본문 페이지 크롤링
 * - HTML 정제
 *
 * ※ 비즈니스 판단 / 저장 책임 없음
 */
@Slf4j
public class NoticeCrawlHelper {

    private static final String BASE_URL = "https://www.mju.ac.kr/";

    /**
     * 공지 목록 페이지 크롤링
     * @param path 공지 목록 path
     * @param page 페이지 번호
     * @return 공지 row 목록
     */
    public static Elements crawlList(String path, int page) {
        String fullUrl = BASE_URL + path + "?page=" + page;
        log.info("[MJS] Requesting URL: {}", fullUrl);

        try {
            Document doc = Jsoup.connect(fullUrl).get();
            return doc.select("tr:not(.headline):not(._artclOdd)");
        } catch (Exception e) {
            throw new IllegalStateException("목록 페이지 크롤링 실패: " + fullUrl, e);
        }
    }

    /**
     * 공지 본문 크롤링
     * @param link 상세 페이지 URL
     * @return 정제된 HTML 본문
     */
    public static String crawlContent(String link) {
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
     * 공지사항 중 본문 section HTML 정제
     */
    private static String cleanNoticeContent(Element contentEl) {

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
}
