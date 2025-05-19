package nova.mjs.news.news;

import nova.mjs.MjsApplication;
import nova.mjs.news.DTO.NewsResponseDTO;
import nova.mjs.news.entity.News;
import nova.mjs.news.service.NewsService;
import nova.mjs.news.exception.NewsNotFoundException;
import nova.mjs.news.repository.NewsRepository;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(classes = MjsApplication.class)
class NewsServiceTest {

    @Autowired
    private NewsService newsService;

    @MockBean
    private NewsRepository newsRepository;

    @Test
    @DisplayName("실제 사이트 HTML 구조가 예상대로 유지되고 있는지 확인")
    void crawlHtmlStructureCheck_live() throws Exception {
        // given
        String testUrl = "https://news.mju.ac.kr/news/articleList.html?sc_section_code=S1N1&page=1";

        // when
        Document doc = Jsoup.connect(testUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(5000)
                .get();
        System.out.println(doc.outerHtml());
        Elements blocks = doc.select("div.article-list div.table-row");
        assertThat(blocks).isNotEmpty(); // 기사 블록이 존재하는지 확인

        for (Element block : blocks) {
            Element titleEl = block.selectFirst("div.list-titles strong"); // ✅ 제목은 strong 내부
            Element datedEl = block.selectFirst("div.list-dated");         // ✅ 기자 및 날짜
            Element summaryEl = block.selectFirst("div.list-summary");

            assertThat(titleEl).isNotNull();
            assertThat(datedEl).isNotNull();

            String title = titleEl.text();     // 기사 제목
            String dated = datedEl.text();     // 기자, 날짜
            String summary = summaryEl != null ? summaryEl.text().trim() : "";  // summary는 optional

            System.out.println("제목: " + title);
            System.out.println("요약: " + summary);
            System.out.println("날짜: " + dated);

            assertThat(title).isNotBlank();
            assertThat(dated).isNotBlank();

        }
    }

    @Test
    @DisplayName("카테고리 기반 뉴스 조회 성공")
    void getNewsByCategory_success() {
        News news = News.createNews(1234L, "제목", "2025-01-01", "기자", "img", "요약", "링크", "REPORT");
        Page<News> page = new PageImpl<>(List.of(news), PageRequest.of(0, 10), 1);

        Mockito.when(newsRepository.findByCategory(eq(News.Category.REPORT), any(Pageable.class)))
                .thenReturn(page);

        Page<NewsResponseDTO> result = newsService.getNewsByCategory("REPORT", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("카테고리 뉴스 없을 경우 예외 발생")
    void getNewsByCategory_notFound() {
        Mockito.when(newsRepository.findByCategory(eq(News.Category.SOCIETY), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(NewsNotFoundException.class, () -> {
            newsService.getNewsByCategory("SOCIETY", PageRequest.of(0, 10));
        });
    }

    @Test
    @DisplayName("카테고리 없이 전체 뉴스 삭제")
    void deleteAllNews_all() {
        Mockito.when(newsRepository.count()).thenReturn(10L);

        assertDoesNotThrow(() -> newsService.deleteAllNews(null));
        Mockito.verify(newsRepository).deleteAll();
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 삭제 시 예외")
    void deleteAllNews_categoryNotFound() {
        Mockito.when(newsRepository.existsByCategory(News.Category.SOCIETY)).thenReturn(false);

        assertThrows(NewsNotFoundException.class, () -> {
            newsService.deleteAllNews("SOCIETY");
        });
    }

    @Test
    @DisplayName("카테고리로 뉴스 삭제")
    void deleteAllNews_byCategory_success() {
        Mockito.when(newsRepository.existsByCategory(News.Category.REPORT)).thenReturn(true);

        assertDoesNotThrow(() -> newsService.deleteAllNews("REPORT"));
        Mockito.verify(newsRepository).deleteByCategory(News.Category.REPORT);
    }
}

