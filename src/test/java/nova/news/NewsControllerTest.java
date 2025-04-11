package nova.news;

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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    private NewsResponseDTO news1;
    private NewsResponseDTO news2;

    @BeforeEach
    void setUp() {
        news1 = NewsResponseDTOFixture.sample();
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
