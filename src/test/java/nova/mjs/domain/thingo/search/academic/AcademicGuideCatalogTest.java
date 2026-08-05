package nova.mjs.domain.thingo.search.academic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AcademicGuideCatalogTest {

    private final AcademicGuideCatalog catalog =
            new AcademicGuideCatalog(new ObjectMapper().findAndRegisterModules());

    @Test
    void loadsStructuredPagesAndVerifiedRules() {
        assertThat(catalog.documents()).hasSize(177);
        assertThat(catalog.documents())
                .anySatisfy(document -> {
                    assertThat(document.getId()).isEqualTo(
                            "2026-2:rule:2025-media-human-academic-foundation");
                    assertThat(document.getType()).isEqualTo("ACADEMIC_GUIDE");
                    assertThat(document.getTitle()).contains("미휴", "학기교", "12학점");
                    assertThat(document.getContent())
                            .contains("적용 학번: 2025학번 이후", "학사안내문 47쪽");
                    assertThat(document.getLink()).endsWith("#guide-page-47-media-human-life");
                });
    }

    @Test
    void keepsPremajorCoursesDistinctFromAcademicFoundationEducation() {
        assertThat(catalog.documents())
                .filteredOn(document -> document.getId().equals(
                        "2026-2:rule:premajor-vs-academic-foundation"))
                .singleElement()
                .extracting(AcademicGuideCatalog.AcademicGuideDocument::getContent)
                .asString()
                .contains("전공이해 기초교과목", "학문기초교양")
                .contains("별개의 졸업 교양요건");
    }
}
