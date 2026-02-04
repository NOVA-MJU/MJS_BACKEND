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

    // 크롤링 최소 연도(포함). 2024년까지 수집하고, 2023 이하는 종료.
    private static final int MIN_YEAR_TO_CRAWL = 2024;

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
                    // ====== [추가] URL 구성 (정렬 파라미터 실험은 여기서) ======
                    // 기존:
                    // String url = BASE_URL + categoryCode + "&view_type=sm&page=" + page;

                    // (추천) 최신순 정렬 파라미터 후보: sc_order_by=E
                    // 실제 사이트가 어떤 파라미터를 쓰는지에 따라 바뀔 수 있습니다.
                    String url = BASE_URL + categoryCode + "&view_type=sm&sc_order_by=E&page=" + page;

                    log.info("[crawl] cat={}, page={}, url={}", cat, page, url);

                    // ====== [추가] User-Agent / timeout / redirect / statusCode 확인 ======
                    org.jsoup.Connection.Response res = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
                            .timeout(10_000)
                            .followRedirects(true)
                            .execute();

                    log.info("[crawl] http status={}, finalUrl={}", res.statusCode(), res.url());

                    Document doc = res.parse();

                    // ====== [추가] 셀렉터별로 기사 블록 개수 비교 ======
                    int sel1 = doc.select(".article-list-content.type-sm .list-block").size();
                    int sel2 = doc.select(".article-list-content .list-block").size();
                    int sel3 = doc.select(".list-block").size();

                    log.info("[crawl] selectorCounts: sel1(type-sm)={}, sel2(article-list-content)={}, sel3(all list-block)={}",
                            sel1, sel2, sel3);

                    // 기존 선택자
                    Elements articles = doc.select(".article-list-content.type-sm .list-block");

                    // ====== [추가] 만약 sel1이 0인데 sel3는 크면, 선택자 문제 가능성 높음 ======
                    // 디버그 편의상, sel1이 0이면 더 넓은 선택자로 fallback (원인 파악 후 정식 수정 권장)
                    if (articles.isEmpty() && sel3 > 0) {
                        log.warn("[crawl] sel1 결과가 비어 fallback 적용: .list-block 기준으로 파싱합니다.");
                        articles = doc.select(".list-block");
                    }

                    if (articles.isEmpty()) {
                        log.warn("페이지 {}에서 더 이상 기사를 찾을 수 없음", page);
                        break;
                    }

                    // ✅ 이번 페이지에서 파싱한 기사 후보를 먼저 모음 (newsIndex로 dedupe)
                    Map<Long, Candidate> candidates = new LinkedHashMap<>();

                    boolean firstLogged = false; // ====== [추가] 첫 기사(또는 첫 파싱 성공 기사)만 상세 로깅 ======

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

                        // 제목
                        String title = article.select(".list-titles a strong").text().trim();

                        // 요약
                        String summary = article.select(".list-summary").text().trim();

                        // 이미지 URL
                        String imageUrl = extractImageUrl(article);

                        // 날짜 & 기자 정보
                        LocalDateTime date = null;
                        String reporter = "기자 정보 없음";

                        String bylineText = article.select(".list-dated").text();

                        // ====== [추가] bylineText가 비어있으면 경고 ======
                        if (bylineText == null || bylineText.isBlank()) {
                            log.warn("[crawl] bylineText empty. page={}, idx={}, link={}", page, newsIndex, link);
                        }

                        if (bylineText != null && !bylineText.isBlank()) {
                            String[] parts = Arrays.stream(bylineText.split("\\|"))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .toArray(String[]::new);

                            if (parts.length >= 2) {
                                String rawDate = parts[parts.length - 1];
                                date = parseDate(rawDate);

                                // ====== [추가] 날짜 파싱 실패 시 rawDate/bylineText 로깅 ======
                                if (date == null) {
                                    log.warn("[crawl] date parse fail. rawDate='{}', bylineText='{}', link={}", rawDate, bylineText, link);
                                }

                                reporter = parts[parts.length - 2];
                            } else if (parts.length == 1) {
                                String rawDate = parts[0];
                                date = parseDate(rawDate);

                                if (date == null) {
                                    log.warn("[crawl] date parse fail. rawDate='{}', bylineText='{}', link={}", rawDate, bylineText, link);
                                }
                            }
                        }

                        if (date == null) {
                            log.warn("날짜 파싱 실패로 기사 스킵(날짜가 존재하지 않음): {}", link);
                            continue;
                        }

                        // ====== [추가] 첫 파싱 성공 기사 1개만 상세로 찍기 (page별) ======
                        if (!firstLogged) {
                            log.info("[crawl] firstArticle page={}, idx={}, date={}, title='{}', byline='{}'",
                                    page, newsIndex, date, title, bylineText);
                            firstLogged = true;
                        }

                        // 2023 이하(= MIN_YEAR_TO_CRAWL 미만) 등장하면 이후는 더 오래된 기사이므로 종료 (최신순 가정)
                        if (date.getYear() < MIN_YEAR_TO_CRAWL) {
                            stop = true;
                            break;
                        }

                        // ✅ 후보 등록 (같은 페이지에서 중복 idx면 최초만 유지)
                        Candidate prev = candidates.putIfAbsent(
                                newsIndex,
                                new Candidate(newsIndex, title, date, reporter, imageUrl, summary, link)
                        );

                        // ====== [추가] 페이지 내 중복 idx 감지 ======
                        if (prev != null) {
                            log.warn("[crawl] 페이지 내 중복 idx 감지: page={}, idx={}, link={}", page, newsIndex, link);
                        }
                    }

                    // stop이 걸렸고 후보가 없으면 종료
                    if (candidates.isEmpty()) {
                        if (stop) break;
                        page++;
                        continue;
                    }

                    // ✅ 페이지 단위로 기존 idx 조회 (쿼리 1번)
                    List<Long> idxList = new ArrayList<>(candidates.keySet());
                    Set<Long> existingIdxSet = new HashSet<>(newsRepository.findExistingNewsIndexIn(idxList));

                    log.info(
                            "page={}, candidatesSize={}, existingSize={}, newSize={}",
                            page,
                            candidates.size(),
                            existingIdxSet.size(),
                            candidates.size() - existingIdxSet.size()
                    );

                    log.info(
                            "page={}, sampleCandidateIdx={}, sampleExistingIdx={}",
                            page,
                            candidates.keySet().stream().limit(5).toList(),
                            existingIdxSet.stream().limit(5).toList()
                    );

                    // ✅ 신규만 엔티티 생성해서 newsList에 추가
                    for (Candidate c : candidates.values()) {
                        if (existingIdxSet.contains(c.newsIndex())) {
                            log.info("이미 존재하는 기사 (newsIndex={}): 크롤링 제외", c.newsIndex());
                            continue;
                        }

                        News news = News.createNews(
                                c.newsIndex(),
                                c.title(),
                                c.date(),
                                c.reporter(),
                                c.imageUrl(),
                                c.summary(),
                                c.link(),
                                cat
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


    // ✅ NewsService 클래스 내부(하단)에 추가
    private record Candidate(
            Long newsIndex,
            String title,
            LocalDateTime date,
            String reporter,
            String imageUrl,
            String summary,
            String link
    ) {}


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
