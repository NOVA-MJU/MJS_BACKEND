/*package nova.mjs.news.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.news.DTO.NewsResponseDTO;
import nova.mjs.news.entity.News;
import nova.mjs.news.repository.NewsRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {
    private final NewsRepository newsRepository;
    private static final String BASE_URL = "https://news.mju.ac.kr/news/articleList.html?sc_section_code=";

    private static final Map<String, String> CATEGORY_CODES = Map.of(
            "보도", "S1N1",
            "사회", "S1N3"
    );

    @Transactional
    public List<NewsResponseDTO> fetchNews(String category) {
        String categoryCode = CATEGORY_CODES.get(category);
        if (categoryCode == null) {
            throw new IllegalArgumentException("잘못된 카테고리입니다.");
        }

        List<News> newsList = new ArrayList<>();
        int page = 1;
        boolean stop = false;

        while (!stop) {
            try {
                String url = BASE_URL + categoryCode + "&view_type=sm&page=" + page;
                Document doc = Jsoup.connect(url).get();
                Elements articles = doc.select(".list-block");

                if (articles.isEmpty()) break; // 더 이상 데이터가 없으면 종료

                for (Element article : articles) {
                    String title = article.select(".titles a").text();
                    String date = article.select(".byline span").get(0).text(); // 날짜
                    String reporter = article.select(".byline span").get(1).text(); // 기자 이름
                    String link = "https://news.mju.ac.kr" + article.select(".titles a").attr("href");
                    String imageUrl = article.select(".thumb img").attr("src");
                    String summary = article.select(".lead").text();

                    // 날짜 필터링 (2025, 2024년 기사만 가져오기)
                    if (!(date.startsWith("2025") || date.startsWith("2024"))) {
                        stop = true;
                        break;
                    }

                    News news = News.createNews(title, date, reporter, imageUrl, summary, link, category);
                    newsList.add(news);
                }
                page++; // 다음 페이지로 이동
            } catch (IOException e) {
                throw new RuntimeException("크롤링 중 오류 발생: " + e.getMessage());
            }
        }

        newsRepository.saveAll(newsList);
        return NewsResponseDTO.fromEntityToList(newsList);
    }
}
*/


package nova.mjs.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.news.DTO.NewsResponseDTO;
import nova.mjs.news.entity.News;
import nova.mjs.news.repository.NewsRepository;
import org.jsoup.Connection;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {
    private final NewsRepository newsRepository;
    private static final String BASE_URL = "https://news.mju.ac.kr/news/articleList.html?sc_section_code=";
    private static final String IMAGE_BASE_URL = "http://news.mju.ac.kr/news/thumbnail/";

    private static final Map<String, String> CATEGORY_CODES = Map.of(
            "REPORT", "S1N1",
            "SOCIETY", "S1N3"
    );

    private static final int MAX_ARTICLES = 5; // 카테고리당 최대 5개 기사만 크롤링
    private static final String DEFAULT_IMAGE_URL = "https://news.mju.ac.kr/default-image.jpg"; // 기본 이미지

    @Transactional
    public List<NewsResponseDTO> crawlAndSaveNews(String category) {
        String categoryCode = CATEGORY_CODES.get(category);
        if (categoryCode == null) {
            throw new IllegalArgumentException("잘못된 카테고리입니다.");
        }

        log.info("기존 '{}' 카테고리의 뉴스 데이터를 삭제합니다.", category);
        //newsRepository.deleteAll();

        List<News> newsList = new ArrayList<>();
        int page = 1;

        log.info("뉴스 크롤링 시작 - 카테고리: {}, 코드: {}", category, categoryCode);

        while (newsList.size() < MAX_ARTICLES) { // 2개 크롤링하면 종료
            try {
                String url = BASE_URL + categoryCode + "&view_type=sm&page=" + page;
                Document doc = Jsoup.connect(url).get();
                Elements articles = doc.select(".list-block");

                if (articles.isEmpty()) {
                    log.warn("페이지 {}에서 기사를 찾을 수 없음", page);
                    break;
                }

                for (Element article : articles) {
                    if (newsList.size() >= MAX_ARTICLES) {
                        break; // 2개를 초과하면 크롤링 중단
                    }

                    //기사 제목 추출
                    String title = article.select(".list-titles a strong").text().trim();
                    if (title.isEmpty()) {
                        log.warn("기사 제목이 비어 있음: {}", article.html());
                        continue; // 빈 제목이면 무시
                    }
                    log.info("기사 제목 : {}", title);

                    // 날짜 & 기자 정보 추출
                    String date = "날짜 정보 없음";  // 기본값 설정
                    String reporter = "기자 정보 없음";  // 기본값 설정
                    Elements byline = article.select(".list-dated");
                    String[] dateInfo = byline.text().split("\\|");

                    date = dateInfo[dateInfo.length - 1].trim(); // 날짜 정보 추출
                    reporter = dateInfo.length > 1 ? dateInfo[1].trim() : null; // 기자 정보가 있으면 추출

                    //기사 링크 추출
                    String link = "https://news.mju.ac.kr" + article.select(".list-titles a").attr("href");

                    // 썸네일 이미지 URL 추출
                    String imageUrl = extractImageUrl(article);

                    log.info("최종 이미지 URL : {}", imageUrl);

                    //기사 요약
                    String summary = article.select(".list-summary").text().trim();

                    // 날짜 필터링 (2025, 2024년 기사만 가져오기)
                    if (!(date.startsWith("2025") || date.startsWith("2024"))) {
                        return NewsResponseDTO.fromEntityToList(newsList); // 날짜 조건 만족하지 않으면 반환
                    }

                    News news = News.createNews(title, date, reporter, imageUrl, summary, link, category);
                    newsList.add(news);
                    log.info("기사 추가: {} - {}", title, date);
                }
                page++; // 다음 페이지로 이동
            } catch (IOException e) {
                log.error("크롤링 중 오류 발생: {}", e.getMessage());
                throw new RuntimeException("크롤링 중 오류 발생: " + e.getMessage());
            }
        }

        if (newsList.isEmpty()) {
            log.warn("카테고리 '{}'에서 수집된 기사가 없음", category);
            throw new RuntimeException("해당 카테고리에서 기사를 찾을 수 없습니다.");
        }

        newsRepository.saveAll(newsList);
        return NewsResponseDTO.fromEntityToList(newsList);
    }

    public List<NewsResponseDTO> getNewsByCategory(String category){
        log.info("'{}' 카테고리 뉴스 조회 요청", category);

        News.Category categoryEnum;
        try {
            categoryEnum = News.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("잘못된 카테고리 입력: " + category);
        }

        List<News> newsList = newsRepository.findByCategory(categoryEnum);

        if (newsList.isEmpty()){
            log.warn("'{}' 카테고리 뉴스 없음", category);
            throw new RuntimeException("해당 카테고리에서 기사를 찾을 수 없습니다.");
        }
        return NewsResponseDTO.fromEntityToList(newsList);
    }


    private String extractImageUrl(Element article) {
        try {
            String imageUrl = article.select(".list-image").attr("style")
                    .replace("background-image:url(", "")
                    .replace(")", "")
                    .replace("\"", "")
                    .trim();

            // 상대 경로를 절대 경로로 변환
            if (imageUrl.startsWith("./thumbnail/")) {
                imageUrl = IMAGE_BASE_URL + imageUrl.substring(11); // "./thumbnail/" 부분 제거 후 절대 경로 추가
            }

            log.info("크롤링된 썸네일 이미지 URL: {}", imageUrl);
            return imageUrl;
        } catch (Exception e) {
            log.warn("썸네일 이미지 추출 실패: {}", e.getMessage());
            return DEFAULT_IMAGE_URL;
        }
    }

}
