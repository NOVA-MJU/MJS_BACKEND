package nova.mjs.domain.thingo.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.news.DTO.NewsResponseDTO;
import nova.mjs.domain.thingo.news.entity.News;
import nova.mjs.domain.thingo.news.exception.NewsNotFoundException;
import nova.mjs.domain.thingo.news.repository.NewsRepository;
import nova.mjs.util.exception.ErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    // 기사 리스트 페이지 기본 URL (섹션 코드만 바꿔서 사용)
    private static final String BASE_URL = "https://news.mju.ac.kr/news/articleList.html?sc_section_code=";

    // 리스트 썸네일 기본 경로
    private static final String IMAGE_BASE_URL = "https://news.mju.ac.kr/news/thumbnail/";

    // 서비스에서 사용하는 카테고리 → 실제 섹션 코드 매핑
    private static final Map<String, String> CATEGORY_CODES = Map.of(
            "REPORT", "S1N1",    // 보도
            "SOCIETY", "S1N3"    // 사회
    );

    // 이미지가 없거나 추출 실패 시 사용할 기본 이미지
    private static final String DEFAULT_IMAGE_URL = "https://news.mju.ac.kr/default-image.jpg";

    /**
     * 명대신문 기사 크롤링 & 저장
     * category = null 이면 REPORT + SOCIETY 모두 크롤링
     */
    @Transactional
    public List<NewsResponseDTO> crawlAndSaveNews(String category) {
        List<String> categoriesToFetch = new ArrayList<>();

        if (category == null) {
            categoriesToFetch.addAll(CATEGORY_CODES.keySet()); // REPORT, SOCIETY 전체
        } else {
            categoriesToFetch.add(category);
        }

        List<NewsResponseDTO> responseList = new ArrayList<>();

        for (String cat : categoriesToFetch) {
            log.info("크롤링 시작: 카테고리 - {}", cat);
            String categoryCode = CATEGORY_CODES.get(cat);

            if (categoryCode == null) {
                throw new IllegalArgumentException("잘못된 카테고리: " + cat);
            }

            List<News> newsList = new ArrayList<>();
            int page = 1;
            boolean stop = false;

            while (!stop) {
                try {
                    // 예) https://news.mju.ac.kr/news/articleList.html?sc_section_code=S1N1&view_type=sm&page=1
                    String url = BASE_URL + categoryCode + "&view_type=sm&page=" + page;
                    Document doc = Jsoup.connect(url).get();
                    Elements articles = doc.select(".article-list-content.type-sm .list-block");

                    if (articles.isEmpty()) {
                        log.warn("페이지 {}에서 더 이상 기사를 찾을 수 없음", page);
                        break;
                    }

                    for (Element article : articles) {
                        String linkPath = article.select(".list-titles a").attr("href");
                        if (linkPath == null || linkPath.isBlank()) {
                            log.warn("링크를 찾을 수 없는 기사 블록, 스킵");
                            continue;
                        }

                        String link = "https://news.mju.ac.kr" + linkPath;
                        Long newsIndex = extractNewsIndex(link);

                        if (newsIndex == null) {
                            log.warn("인덱스에 맞는 기사를 찾을 수 없음 : {}", link);
                            continue;
                        }

                        log.info("기사 발견 : {}", newsIndex);

                        // 이미 저장된 기사면 스킵
                        if (newsRepository.existsByNewsIndex(newsIndex)) {
                            log.info("이미 존재하는 기사 (newsIndex={}): 크롤링 제외", newsIndex);
                            continue;
                        }

                        // 제목
                        String title = article.select(".list-titles a strong").text().trim();

                        // 요약
                        String summary = article.select(".list-summary").text().trim();

                        // 이미지 URL (없으면 기본 이미지)
                        String imageUrl = extractImageUrl(article);

                        // 날짜 & 기자 정보
                        LocalDateTime date = null;
                        String reporter = "기자 정보 없음";

                        Elements byline = article.select(".list-dated");
                        String bylineText = byline.text();

                        if (!bylineText.isBlank()) {
                            // "보도 | 김지은 사회문화부 정기자 | 2025-11-10 02:06"
                            // "탑 | 권지민 대학보도부 정기자 | 2025-10-13 01:15"
                            // "보도기획 | 정승원 수습기자, 홍성범 수습기자 | 2025-10-13 01:15"
                            String[] parts = Arrays.stream(bylineText.split("\\|"))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .toArray(String[]::new);

                            if (parts.length >= 2) {
                                String rawDate = parts[parts.length - 1];          // 마지막 = 날짜
                                date = parseDate(rawDate);
                                reporter = parts[parts.length - 2];                // 그 앞 = 기자/부서 or '보도', '탑' 등
                            } else if (parts.length == 1) {
                                // 혹시 날짜만 있는 케이스 대비
                                String rawDate = parts[0];
                                date = parseDate(rawDate);
                            }
                        }

                        // 연도 필터 (2024, 2025만 유지 / 그 이하 나오면 크롤링 종료)
                        if (date == null || !(date.getYear() == 2025 || date.getYear() == 2024)) {
                            if (!newsList.isEmpty()) {
                                log.info("2023년 이하 기사 발견! 저장 후 크롤링 종료: {}개 기사 저장", newsList.size());
                                newsRepository.saveAll(newsList);
                                newsList.clear();
                            }
                            stop = true;
                            break;
                        }

                        News news = News.createNews(
                                newsIndex,
                                title,
                                date,
                                reporter,
                                imageUrl,
                                summary,
                                link,
                                cat // News.Category와 매핑되는 문자열(예: REPORT, SOCIETY)
                        );

                        newsList.add(news);
                    }

                    page++;
                } catch (IOException e) {
                    throw new RuntimeException("크롤링 중 오류 발생: " + e.getMessage(), e);
                }
            }

            // 남은 기사 저장
            if (!newsList.isEmpty()) {
                log.info("카테고리 '{}' - {}개의 뉴스 저장 시작", cat, newsList.size());
                newsRepository.saveAll(newsList);
                log.info("저장 완료: '{}' 카테고리 {}개의 뉴스", cat, newsList.size());

                responseList.addAll(NewsResponseDTO.fromEntityToList(newsList));
            }
        }

        return responseList;
    }

    /**
     * 날짜 파싱
     * 지원 포맷:
     *  - yyyy.MM.dd HH:mm
     *  - yyyy-MM-dd HH:mm
     *  - yyyy.MM.dd
     *  - yyyy-MM-dd
     */
    private LocalDateTime parseDate(String rawDate) {
        List<String> patterns = List.of(
                "yyyy.MM.dd HH:mm",
                "yyyy-MM-dd HH:mm",
                "yyyy.MM.dd",
                "yyyy-MM-dd"
        );
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (pattern.contains("HH:mm")) {
                    return LocalDateTime.parse(rawDate, formatter);
                } else {
                    // 날짜만 있는 경우 " 00:00"을 붙여서 자정 기준으로 파싱
                    return LocalDateTime.parse(
                            rawDate + " 00:00",
                            DateTimeFormatter.ofPattern(pattern + " HH:mm")
                    );
                }
            } catch (Exception ignored) {
            }
        }
        log.warn("지원하지 않는 날짜 형식 : {}", rawDate);
        return null;
    }

    /**
     * 기사 링크에서 idxno 추출
     * 예: /news/articleView.html?idxno=13557 → 13557
     */
    private Long extractNewsIndex(String url) {
        Pattern pattern = Pattern.compile("idxno=(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    /**
     * 리스트 썸네일 이미지 URL 추출
     *  - style="background-image:url(./thumbnail/202511/....jpg)"
     *  - 이미지 태그가 없거나 style 없으면 DEFAULT_IMAGE_URL
     */
    private String extractImageUrl(Element article) {
        try {
            Element imageDiv = article.selectFirst(".list-image");
            if (imageDiv == null) {
                // 이미지 없는 기사(텍스트만 있는 경우) → 기본 이미지 사용
                return DEFAULT_IMAGE_URL;
            }

            String style = imageDiv.attr("style");
            if (style == null || style.isBlank()) {
                return DEFAULT_IMAGE_URL;
            }

            String imageUrl = style
                    .replace("background-image:url(", "")
                    .replace(")", "")
                    .replace("\"", "")
                    .trim();

            // "./thumbnail/202511/..." 형태를 절대 경로로 변경
            if (imageUrl.startsWith("./thumbnail/")) {
                imageUrl = IMAGE_BASE_URL + imageUrl.substring("./thumbnail/".length());
            } else if (imageUrl.startsWith("/news/thumbnail/")) {
                // 사이드 포토/인기뉴스처럼 /news/thumbnail/... 로 나오는 경우도 대비
                imageUrl = "https://news.mju.ac.kr" + imageUrl;
            }

            return imageUrl.isBlank() ? DEFAULT_IMAGE_URL : imageUrl;
        } catch (Exception e) {
            log.warn("썸네일 이미지 추출 실패: {}", e.getMessage());
            return DEFAULT_IMAGE_URL;
        }
    }

    /**
     * 카테고리별/전체 뉴스 페이지 조회
     */
    public Page<NewsResponseDTO> getNewsByCategory(String category, Pageable pageable) {
        log.info("뉴스 조회 요청, category='{}'", category);

        // ALL 또는 null이면 전체 조회
        if (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category)) {
            Page<News> newsPage = newsRepository.findAll(pageable);
            if (newsPage.isEmpty()) {
                log.warn("전체 뉴스 없음");
                throw new NewsNotFoundException(ErrorCode.NEWS_NOT_FOUND);
            }
            return newsPage.map(NewsResponseDTO::fromEntity);
        }

        // 단일 카테고리 조회
        News.Category categoryEnum;
        try {
            categoryEnum = News.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("잘못된 카테고리 입력: " + category);
        }

        Page<News> newsPage = newsRepository.findByCategory(categoryEnum, pageable);
        if (newsPage.isEmpty()) {
            log.warn("'{}' 카테고리 뉴스 없음", category);
            throw new NewsNotFoundException(ErrorCode.NEWS_NOT_FOUND);
        }
        return newsPage.map(NewsResponseDTO::fromEntity);
    }

    /**
     * 뉴스 데이터 삭제 (전체 또는 카테고리별)
     */
    @Transactional
    public void deleteAllNews(String category) {
        if (category == null) {
            if (newsRepository.count() == 0) {
                throw new NewsNotFoundException("저장된 기사가 없습니다.", ErrorCode.NEWS_NOT_FOUND);
            }
            log.info("모든 기사 데이터를 삭제합니다.");
            newsRepository.deleteAll();
        } else {
            News.Category categoryEnum = News.Category.valueOf(category.toUpperCase());
            if (!newsRepository.existsByCategory(categoryEnum)) {
                throw new NewsNotFoundException("해당 카테고리의 기사가 없습니다.", ErrorCode.NEWS_NOT_FOUND);
            }
            log.info("'{}' 카테고리의 기사 데이터를 삭제합니다.", category);
            newsRepository.deleteByCategory(categoryEnum);
        }
    }
}
