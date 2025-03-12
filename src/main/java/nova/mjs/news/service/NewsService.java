package nova.mjs.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.news.DTO.NewsResponseDTO;
import nova.mjs.news.entity.News;
import nova.mjs.news.exception.NewsNotFoundException;
import nova.mjs.news.repository.NewsRepository;
import nova.mjs.util.exception.ErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;
    private static final String BASE_URL = "https://news.mju.ac.kr/news/articleList.html?sc_section_code=";
    private static final String IMAGE_BASE_URL = "http://news.mju.ac.kr/news/thumbnail/";

    private static final Map<String, String> CATEGORY_CODES = Map.of(
            "REPORT", "S1N1",
            "SOCIETY", "S1N3"
    );

    private static final String DEFAULT_IMAGE_URL = "https://news.mju.ac.kr/default-image.jpg"; // 기본 이미지

    @Transactional
    public List<NewsResponseDTO> crawlAndSaveNews(String category) {
        List<String> categoriesToFetch = new ArrayList<>();

        if (category == null) {
            // 카테고리를 지정하지 않으면 REPORT와 SOCIETY 자동 크롤링
            categoriesToFetch.addAll(CATEGORY_CODES.keySet());
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
                    String url = BASE_URL + categoryCode + "&view_type=sm&page=" + page;
                    Document doc = Jsoup.connect(url).get();
                    Elements articles = doc.select(".list-block");

                    if (articles.isEmpty()) {
                        log.warn("페이지 {}에서 더 이상 기사를 찾을 수 없음", page);
                        break;
                    }

                    for (Element article : articles) {
                        String link = "https://news.mju.ac.kr" + article.select(".list-titles a").attr("href");

                        Long newsIndex = extractNewsIndex(link);
                        log.info("기사 발견 : {}", newsIndex);
                        if (newsIndex == null) {
                            log.warn("인덱스에 맞는 기사를 찾을 수 없음 : {}", link);
                            continue;
                        }

                        if (newsRepository.existsByNewsIndex(newsIndex)) {
                            log.info("이미 존재하는 기사 (newsIndex={}): 크롤링 제외", newsIndex);
                            continue; // 해당 기사만 제외하고 계속 크롤링 진행
                        }

                        //기사 정보 추출
                        String title = article.select(".list-titles a strong").text().trim(); //제목
                        String imageUrl = extractImageUrl(article); //이미지 url
                        String summary = article.select(".list-summary").text().trim(); //헤더 요약

                        // 날짜 & 기자 정보 추출
                        String date = "날짜 정보 없음";
                        String reporter = "기자 정보 없음";
                        Elements byline = article.select(".list-dated");

                        //dateInfo에서 날짜와 기자 정보 추출
                        String[] dateInfo = byline.text().split("\\|");

                        if (dateInfo.length > 0) {
                            date = dateInfo[dateInfo.length - 1].trim();
                        }
                        if (dateInfo.length > 1) {
                            reporter = dateInfo[1].trim();
                        }

                        // 크롤링 중단 전, 수집된 기사 저장
                        if (!(date.startsWith("2025") || date.startsWith("2024"))) {
                            if (!newsList.isEmpty()) {  // 이전까지 크롤링한 데이터 저장
                                log.info("2023년 이하 기사 발견! 저장 후 크롤링 종료: {}개 기사 저장", newsList.size());
                                newsRepository.saveAll(newsList);
                                newsList.clear();  // 리스트 초기화
                            }
                            stop = true;
                            break;
                        }

                        News news = News.createNews(newsIndex, title, date, reporter, imageUrl, summary, link, cat);
                        newsList.add(news);
                    }
                    page++;
                } catch (IOException e) {
                    throw new RuntimeException("크롤링 중 오류 발생: " + e.getMessage());
                }
            }

            // 크롤링이 끝날 때마다 DB에 저장
            if (!newsList.isEmpty()) {
                log.info("카테고리 '{}' - {}개의 뉴스 저장 시작", cat, newsList.size());
                newsRepository.saveAll(newsList);
                log.info("저장 완료: '{}' 카테고리 {}개의 뉴스", cat, newsList.size());

                // 저장된 뉴스 리스트를 DTO로 변환하여 응답 리스트에 추가
                responseList.addAll(NewsResponseDTO.fromEntityToList(newsList));
            }
        }

        return responseList; // 각 카테고리별로 저장된 뉴스 목록 반환
    }

    private Long extractNewsIndex(String url){
        Pattern pattern = Pattern.compile("idxno=(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()){
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    private String extractImageUrl(Element article) {
        try {
            String imageUrl = article.select(".list-image").attr("style")
                    .replace("background-image:url(", "")
                    .replace(")", "")
                    .replace("\"", "")
                    .trim();

            if (imageUrl.startsWith("./thumbnail/")) {
                imageUrl = IMAGE_BASE_URL + imageUrl.substring(11);
            }

            return imageUrl;
        } catch (Exception e) {
            log.warn("썸네일 이미지 추출 실패: {}", e.getMessage());
            return DEFAULT_IMAGE_URL;
        }
    }

    public List<NewsResponseDTO> getNewsByCategory(String category) {
        log.info("'{}' 카테고리 뉴스 조회 요청", category);

        News.Category categoryEnum;

        try {
            categoryEnum = News.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("잘못된 카테고리 입력: " + category);
        }

        List<News> newsList = newsRepository.findByCategory(categoryEnum);

        if (newsList.isEmpty()) {
            log.warn("'{}' 카테고리 뉴스 없음", category);
            throw new NewsNotFoundException("해당 카테고리에서 기사를 찾을 수 없습니다.", ErrorCode.NEWS_NOT_FOUND);
        }
        return NewsResponseDTO.fromEntityToList(newsList);
    }

    @Transactional
    public void deleteAllNews(String category){
        if (category == null) {
            if (newsRepository.count() == 0){
                throw new NewsNotFoundException("저장된 기사가 없습니다.", ErrorCode.NEWS_NOT_FOUND);
            }
            log.info("모든 기사 데이터를 삭제합니다.");
            newsRepository.deleteAll();
        } else {
            News.Category categoryEnum = News.Category.valueOf(category.toUpperCase());
            if(!newsRepository.existsByCategory(categoryEnum)){
                throw new NewsNotFoundException("해당 카테고리의 기사가 없습니다.", ErrorCode.NEWS_NOT_FOUND);
            }
            log.info("'{}' 카테고리의 기사 데이터를 삭제합니다.", category);
            newsRepository.deleteByCategory(News.Category.valueOf(category.toUpperCase()));
        }

    }
}
