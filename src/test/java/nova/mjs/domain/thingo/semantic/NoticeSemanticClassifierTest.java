package nova.mjs.domain.thingo.semantic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeSemanticClassifierTest {

    private NoticeSemanticClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new NoticeSemanticClassifier(new TopicCatalog());
    }

    @Test
    @DisplayName("하위 Topic과 부모 Topic을 함께 저장하고 Event Type은 별도 분류한다")
    void classifiesTopicHierarchyAndEventType() {
        Instant deadline = Instant.parse("2026-07-16T14:59:59Z");

        var result = classifier.classify(
                "2026학년도 학사학위취득유예 신청 안내",
                "졸업예정자는 7월 16일까지 신청 바랍니다.",
                deadline);

        assertThat(result.directTopicIds()).containsExactly("GRADUATION_DEFERRAL");
        assertThat(result.expandedTopicIds()).contains("GRADUATION", "GRADUATION_DEFERRAL");
        assertThat(result.eventType()).isEqualTo(NoticeEventType.APPLICATION);
        assertThat(result.audiences()).contains(NoticeAudience.GRADUATION_CANDIDATE);
        assertThat(result.deadline()).isEqualTo(deadline);
        assertThat(result.classificationVersion()).isEqualTo(3);
        assertThat(result.classificationSource()).isEqualTo(ClassificationSource.RULE);
        assertThat(result.classificationConfidence()).isEqualTo(0.95d);
    }

    @Test
    @DisplayName("사용자가 명시하지 않은 대상·캠퍼스를 ALL로 임의 축소하지 않는다")
    void leavesUnknownFiltersUnspecified() {
        var result = classifier.classify("국취 모집 안내", "신청 방법을 확인하세요.", null);

        assertThat(result.topicIds()).contains("EMPLOYMENT_SUPPORT", "NATIONAL_EMPLOYMENT_SUPPORT");
        assertThat(result.audiences()).isEmpty();
        assertThat(result.campuses()).isEmpty();
    }
}
