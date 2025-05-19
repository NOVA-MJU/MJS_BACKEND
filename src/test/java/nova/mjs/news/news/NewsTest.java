package nova.mjs.news.news;

import nova.mjs.news.entity.News;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class NewsTest {

    @Test
    @DisplayName("News 정적 팩토리 메서드로 엔티티 생성 성공")
    void createNews_success() {
        // given
        Long newsIndex = 1001L;
        String title = "테스트 뉴스";
        String date = "2025-05-08";
        String reporter = "김기자";
        String imageUrl = "https://example.com/image.png";
        String summary = "이것은 뉴스 요약입니다.";
        String link = "https://example.com/news/1001";
        String category = "report"; // 소문자도 허용

        // when
        News news = News.createNews(newsIndex, title, date, reporter, imageUrl, summary, link, category);

        // then
        assertThat(news.getNewsIndex()).isEqualTo(newsIndex);
        assertThat(news.getTitle()).isEqualTo(title);
        assertThat(news.getDate()).isEqualTo(date);
        assertThat(news.getReporter()).isEqualTo(reporter);
        assertThat(news.getImageUrl()).isEqualTo(imageUrl);
        assertThat(news.getSummary()).isEqualTo(summary);
        assertThat(news.getLink()).isEqualTo(link);
        assertThat(news.getCategory()).isEqualTo(News.Category.REPORT);
    }

    @Nested
    @DisplayName("Category 변환 테스트")
    class CategoryParsingTest {

        @Test
        @DisplayName("대소문자 허용 - report → REPORT")
        void parseLowercaseCategory() {
            assertThat(News.Category.fromStringTOUppercase("report")).isEqualTo(News.Category.REPORT);
        }

        @Test
        @DisplayName("잘못된 카테고리 입력 시 예외 발생")
        void invalidCategory_throwsException() {
            assertThatThrownBy(() -> News.Category.fromStringTOUppercase("invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid category value");
        }

        @Test
        @DisplayName("null 입력 시 예외 발생")
        void nullCategory_throwsException() {
            assertThatThrownBy(() -> News.Category.fromStringTOUppercase(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("빈 문자열 입력 시 예외 발생")
        void blankCategory_throwsException() {
            assertThatThrownBy(() -> News.Category.fromStringTOUppercase("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or blank");
        }
    }
}
