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
        assertThat(result.classificationVersion()).isEqualTo(4);
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

    @Test
    @DisplayName("본문의 일반 해외 프로그램 언급만으로 다른 주제 공지를 오분류하지 않는다")
    void ignoresGenericGlobalTopicMentionsInBody() {
        var exchangeMention = classifier.classify(
                "체육시설 운영 안내",
                "교환학생도 체육시설을 이용할 수 있습니다.",
                null);
        var trainingMention = classifier.classify(
                "반도체 융합전공 참여학생 모집",
                "참여자는 추후 어학연수 프로그램도 확인할 수 있습니다.",
                null);
        var volunteerMention = classifier.classify(
                "세계시민교육 특강 안내",
                "해외봉사 사례를 포함한 시민교육 강좌입니다.",
                null);

        assertThat(exchangeMention.directTopicIds()).isEmpty();
        assertThat(trainingMention.directTopicIds()).isEmpty();
        assertThat(volunteerMention.directTopicIds()).isEmpty();
    }

    @Test
    @DisplayName("본문에만 있는 고유 프로그램명은 보조 근거로 분류한다")
    void keepsDistinctiveProgramNameFromBody() {
        var result = classifier.classify(
                "일본 호텔 전문가 양성과정 모집",
                "K-Move 스쿨 일본취업연수 과정입니다.",
                null);

        assertThat(result.directTopicIds()).containsExactly("K_MOVE_PROGRAM");
    }
}
