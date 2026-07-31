package nova.mjs.domain.thingo.keywordAlarm.service;

import nova.mjs.domain.thingo.semantic.TopicCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlarmTopicServiceTest {

    private AlarmTopicService service;

    @BeforeEach
    void setUp() {
        service = new AlarmTopicService(new TopicCatalog());
    }

    @Test
    @DisplayName("줄임말로 검색해도 표준 Topic을 반환한다")
    void autocompleteByAlias() {
        var response = service.autocomplete("국취", 8);

        assertThat(response.query()).isEqualTo("국취");
        assertThat(response.items())
                .extracting(item -> item.topicId())
                .containsExactly("NATIONAL_EMPLOYMENT_SUPPORT");
    }

    @Test
    @DisplayName("구독 불가능 Topic은 자동완성에서 제외한다")
    void excludesNonSubscribableTopic() {
        var response = service.autocomplete("졸업", 20);

        assertThat(response.items())
                .extracting(item -> item.topicId())
                .contains("GRADUATION", "GRADUATION_REQUIREMENTS", "GRADUATION_DEFERRAL")
                .doesNotContain("GRADUATION_ART_EVENT");
    }

    @Test
    @DisplayName("상위 Topic 구독은 하위 Topic 범위를 포함한다")
    void expandsParentSubscription() {
        assertThat(service.inclusiveTopicIds("EMPLOYMENT_SUPPORT"))
                .contains("EMPLOYMENT_SUPPORT", "NATIONAL_EMPLOYMENT_SUPPORT",
                        "UNIVERSITY_JOB_PLUS_CENTER", "STUDENT_CUSTOM_EMPLOYMENT_SERVICE", "JOB_PREPARATION");
    }

    @Test
    @DisplayName("해외 입력 시 실제 프로그램과 직관적인 상위 범위를 자동완성한다")
    void autocompleteActualGlobalPrograms() {
        var response = service.autocomplete("해외", 20);

        assertThat(response.items())
                .extracting(item -> item.topicId())
                .contains("GLOBAL_PROGRAM", "GLOBAL_WORK_EXPERIENCE", "GLOBAL_EMPLOYMENT",
                        "STUDY_ABROAD", "SHORT_TERM_OVERSEAS_TRAINING", "OVERSEAS_VOLUNTEERING");

        assertThat(service.autocomplete("WELL", 8).items())
                .extracting(item -> item.topicId())
                .containsExactly("WELL_PROGRAM");
        assertThat(service.autocomplete("WEST", 8).items())
                .extracting(item -> item.topicId())
                .containsExactly("WEST_PROGRAM");
    }

    @Test
    @DisplayName("해외·국제 프로그램 전체 구독은 확인된 모든 세부 프로그램을 포함한다")
    void expandsGlobalProgramSubscription() {
        assertThat(service.inclusiveTopicIds("GLOBAL_PROGRAM"))
                .contains("GLOBAL_PROGRAM", "WELL_PROGRAM", "WEST_PROGRAM", "WSP_PROGRAM",
                        "K_MOVE_PROGRAM", "EXCHANGE_VISITING_STUDENT", "DUAL_DEGREE_PROGRAM",
                        "SAF_PROGRAM", "ERASMUS_PLUS_PROGRAM", "STUDY_ABROAD_SCHOLARSHIP",
                        "SHORT_TERM_OVERSEAS_TRAINING", "OVERSEAS_VOLUNTEERING");
    }
}
