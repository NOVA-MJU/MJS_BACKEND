package nova.mjs.domain.thingo.semantic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TopicCatalogTest {

    private TopicCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new TopicCatalog();
    }

    @Test
    @DisplayName("약어와 표준어를 같은 Topic으로 해석한다")
    void resolvesAbbreviationsAndCanonicalTerms() {
        assertSameTopic("졸특", "졸업생 취업지원", "GRADUATE_CAREER");
        assertSameTopic("국취", "국민취업지원제도", "NATIONAL_EMPLOYMENT_SUPPORT");
        assertSameTopic("대플", "대학일자리플러스센터", "UNIVERSITY_JOB_PLUS_CENTER");
        assertSameTopic("취준", "취업 준비", "JOB_PREPARATION");
    }

    @Test
    @DisplayName("가운뎂점·점·괄호·슬래시 표현을 같은 Topic으로 해석한다")
    void resolvesPunctuationVariants() {
        assertVariants("LEAVE_RETURN", "휴학", "복학", "휴·복학", "휴/복학", "휴 복학");
        assertVariants("NEW_TRANSFER_ADMISSION", "신입학", "편입학", "신(편)입", "신·편입학");
        assertVariants("STUDENT_CUSTOM_EMPLOYMENT_SERVICE", "재맞고", "재.맞.고");
    }

    @Test
    @DisplayName("하위 Topic이 매칭되면 부모 Topic의 부분 문자열 매칭은 제거한다")
    void keepsMostSpecificTopic() {
        assertThat(topicIds("국민취업지원제도 신청"))
                .containsExactly("NATIONAL_EMPLOYMENT_SUPPORT")
                .doesNotContain("EMPLOYMENT_SUPPORT");
        assertThat(topicIds("졸업유예 신청"))
                .containsExactly("GRADUATION_DEFERRAL")
                .doesNotContain("GRADUATION");
    }

    @Test
    @DisplayName("한 글자 축약어는 오탐 방지를 위해 해석하지 않는다")
    void rejectsOneLetterAliases() {
        assertThat(catalog.resolve("졸 신청")).isEmpty();
    }

    @Test
    @DisplayName("상위 Topic은 하위 Topic 전체를 포함하고 하위 Topic은 자신만 포함한다")
    void expandsHierarchyForSubscription() {
        assertThat(catalog.inclusiveTopicIds("GRADUATION"))
                .contains("GRADUATION", "GRADUATION_REQUIREMENTS", "GRADUATION_DEFERRAL",
                        "EARLY_GRADUATION", "DEGREE_CEREMONY", "GRADUATE_CAREER", "GRADUATION_ART_EVENT");
        assertThat(catalog.inclusiveTopicIds("GRADUATION_DEFERRAL"))
                .containsExactly("GRADUATION_DEFERRAL");
    }

    private void assertSameTopic(String left, String right, String topicId) {
        assertThat(topicIds(left)).containsExactly(topicId);
        assertThat(topicIds(right)).containsExactly(topicId);
    }

    private void assertVariants(String topicId, String... variants) {
        for (String variant : variants) {
            assertThat(topicIds(variant)).as(variant).containsExactly(topicId);
        }
    }

    private List<String> topicIds(String text) {
        return catalog.resolve(text).stream().map(match -> match.topic().topicId()).toList();
    }
}
