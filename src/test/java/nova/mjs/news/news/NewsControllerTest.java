package nova.mjs.news.news;

import nova.mjs.MjsApplication;
import nova.mjs.news.DTO.NewsResponseDTO;
import nova.mjs.news.entity.News;
import nova.mjs.news.service.NewsService;
import nova.util.dto.NewsResponseDTOFixture;
import nova.mjs.util.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MjsApplication.class)
@AutoConfigureMockMvc
public class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService; //서비스 mock 객체 - 가짜 객체

    private NewsResponseDTO news1;
    private NewsResponseDTO news2;

    @BeforeEach
    void setUp() {
        news1 = NewsResponseDTOFixture.sample(); //랜덤 생성된 newsResponseDTO
        news2 = NewsResponseDTOFixture.sample();
    }

    @Test
    @DisplayName("뉴스 목록 조회 - REPORT 카테고리")
    void testGetNewsByCategory() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<NewsResponseDTO> mockPage = new PageImpl<>(List.of(news1, news2), pageable, 2);

        when(newsService.getNewsByCategory("REPORT", pageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/news")
                        .param("category", "REPORT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].title").value(news1.getTitle()))
                .andExpect(jsonPath("$.data.content[1].title").value(news2.getTitle()));
    }

    @Test
    @DisplayName("뉴스 목록 조회 - SOCIETY 카테고리")
    void testGetNewsBySocietyCategory() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<NewsResponseDTO> mockPage = new PageImpl<>(List.of(news1, news2), pageable, 2);

        when(newsService.getNewsByCategory("SOCIETY", pageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/news")
                        .param("category", "SOCIETY")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].title").value(news1.getTitle()))
                .andExpect(jsonPath("$.data.content[1].title").value(news2.getTitle()));
    }

    @Test
    @DisplayName("뉴스 목록 조회 - 다수 뉴스, 페이지 넘김")
    void testGetNewsWithPagination() throws Exception {
        // 뉴스 30개를 생성합니다 (0 ~ 29)
        List<NewsResponseDTO> newsList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            NewsResponseDTO news = NewsResponseDTOFixture.sample();
            newsList.add(news);
        }

        // 2페이지 요청이라고 가정: 페이지 번호 1 (0부터 시작), size 10
        Pageable pageable = PageRequest.of(1, 10); // 1번 페이지, 한 페이지당 10개

        // 2번째 페이지에 해당하는 데이터 (index 10~19)
        List<NewsResponseDTO> pageContent = newsList.subList(10, 20);

        Page<NewsResponseDTO> mockPage = new PageImpl<>(pageContent, pageable, newsList.size());

        when(newsService.getNewsByCategory("REPORT", pageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/news")
                        .param("category", "REPORT")
                        .param("page", "1")  // 여기 주의!! page=1
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(30)) // 전체는 30개
                .andExpect(jsonPath("$.data.content.length()").value(10)) // 현재 페이지에는 10개만
                .andExpect(jsonPath("$.data.content[0].title").value(pageContent.get(0).getTitle()))
                .andExpect(jsonPath("$.data.content[9].title").value(pageContent.get(9).getTitle()));
    }

    @Test
    @DisplayName("뉴스 목록 조회 - 마지막 페이지 데이터 부족")
    void testGetNews_LastPageWithLessData() throws Exception {
        // 뉴스 25개 생성 (0 ~ 24)
        List<NewsResponseDTO> newsList = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            NewsResponseDTO news = NewsResponseDTOFixture.sample();
            newsList.add(news);
        }

        // 3번 페이지 요청 (page=2), size=10
        // 0~9: 0번 페이지
        // 10~19: 1번 페이지
        // 20~24: 2번 페이지 (5개만 남음)
        Pageable pageable = PageRequest.of(2, 10);

        List<NewsResponseDTO> pageContent = newsList.subList(20, 25); // 인덱스 20~24

        Page<NewsResponseDTO> mockPage = new PageImpl<>(pageContent, pageable, newsList.size());

        when(newsService.getNewsByCategory("REPORT", pageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/news")
                        .param("category", "REPORT")
                        .param("page", "2")  // 여기 주의!! page=2 (0부터 시작)
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(25)) // 전체는 25개
                .andExpect(jsonPath("$.data.content.length()").value(5)) // 현재 페이지에는 5개만
                .andExpect(jsonPath("$.data.content[0].title").value(pageContent.get(0).getTitle()))
                .andExpect(jsonPath("$.data.content[4].title").value(pageContent.get(4).getTitle()));
    }

    @Test
    @DisplayName("뉴스 크롤링 및 저장 - 카테고리 없이 전체 크롤링")
    void testCrawlAndSaveNews_AllCategories() throws Exception {
        List<NewsResponseDTO> crawledNews = List.of(news1, news2);
        when(newsService.crawlAndSaveNews(null)).thenReturn(crawledNews);

        mockMvc.perform(post("/api/v1/news/fetch"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].link").value(news1.getLink()))
                .andExpect(jsonPath("$.data[1].link").value(news2.getLink()));
    }

    @Test
    @DisplayName("뉴스 전체 삭제 - 카테고리 없이 전체 삭제")
    void testDeleteAllNews() throws Exception {
        mockMvc.perform(delete("/api/v1/news/delete"))
                .andExpect(status().isOk());
    }
}
