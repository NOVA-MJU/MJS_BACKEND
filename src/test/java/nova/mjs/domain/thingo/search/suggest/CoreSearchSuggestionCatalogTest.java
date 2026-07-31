package nova.mjs.domain.thingo.search.suggest;

import nova.mjs.domain.thingo.semantic.TopicCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoreSearchSuggestionCatalogTest {

    private CoreSearchSuggestionCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new CoreSearchSuggestionCatalog(new TopicCatalog());
    }

    @Test
    @DisplayName("장학 검색은 공지 제목이 아닌 표준 핵심어를 반환한다")
    void scholarshipReturnsCoreKeywordsOnly() {
        var result = catalog.suggest("장학", 10);

        assertThat(result)
                .contains("장학", "국가장학", "교내장학", "장학생", "한국장학재단")
                .allMatch(value -> !value.contains("2026년"))
                .allMatch(value -> !value.contains("모집 안내"));
    }

    @Test
    @DisplayName("등록된 약어는 Topic의 표준 핵심어로 확장한다")
    void topicAliasReturnsCanonicalKeywords() {
        assertThat(catalog.suggest("국취", 10))
                .contains("국민취업지원제도", "국민취업지원", "국취");
        assertThat(catalog.suggest("재.맞", 10))
                .contains("재학생맞춤형고용서비스", "재맞고");
    }

    @Test
    @DisplayName("한 글자 입력과 미등록 검색어는 억지로 확장하지 않는다")
    void avoidsLowConfidenceFallback() {
        assertThat(catalog.suggest("졸", 10)).isEmpty();
        assertThat(catalog.suggest("완전히미등록된표현", 10)).isEmpty();
    }

    @Test
    @DisplayName("특수문자 변형에서도 표준 핵심어를 반환한다")
    void punctuationVariantsResolve() {
        assertThat(catalog.suggest("휴·복", 10))
                .contains("휴학·복학", "휴학", "복학", "휴복학");
        assertThat(catalog.suggest("신(편)입", 10))
                .contains("신입학·편입학", "신입학", "편입학", "신편입");
    }
}
