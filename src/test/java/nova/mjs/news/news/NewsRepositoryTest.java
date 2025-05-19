package nova.mjs.news.news;

import nova.mjs.MjsApplication;
import nova.mjs.news.entity.News;
import nova.mjs.news.entity.News.Category;
import nova.mjs.news.repository.NewsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MjsApplication.class)
@Transactional // 테스트 이후 DB 롤백되도록 보장
class NewsRepositoryTest {

    @Autowired
    private NewsRepository newsRepository;

    @Test
    @DisplayName("뉴스 저장 및 조회 테스트")
    void saveAndFindNews() {
        // given
        News news = News.createNews(
                1234L,
                "테스트 뉴스 제목",
                "2025-05-08",
                "기자명",
                "https://img.test.com/news.jpg",
                "기사 요약입니다",
                "https://news.test.com/article/1234",
                "REPORT"
        );

        // when
        News saved = newsRepository.save(news);
        Optional<News> found = newsRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("테스트 뉴스 제목");
        assertThat(found.get().getCategory()).isEqualTo(Category.REPORT);
    }
}


