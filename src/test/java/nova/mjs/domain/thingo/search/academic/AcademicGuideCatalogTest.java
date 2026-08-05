package nova.mjs.domain.thingo.search.academic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AcademicGuideCatalogTest {

    private final AcademicGuideCatalog catalog =
            new AcademicGuideCatalog(new ObjectMapper().findAndRegisterModules());

    @Test
    void loadsStructuredPagesAndVerifiedRules() {
        assertThat(catalog.documents()).hasSizeGreaterThanOrEqualTo(190);
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
    void exposesMediaHumanAcademicFoundationRulesByAdmissionYear() {
        assertThat(catalog.documents())
                .filteredOn(document -> document.getId().equals(
                        "2026-2:rule:media-human-academic-foundation-by-admission-year"))
                .singleElement()
                .satisfies(document -> {
                    assertThat(document.getTitle()).contains("미휴 학기교", "학번별 필요 학점");
                    assertThat(document.getContent())
                            .contains("2009~2014학번", "2015~2017학번", "2018~2024학번", "2025학번 이후")
                            .contains("필요 학점: 12학점")
                            .contains("대중문화와매스컴(3)", "다문화사회의이해(3)");
                });
    }

    @Test
    void mapsDepartmentsToTheirCollegeForAcademicFoundationSearch() {
        assertThat(catalog.documents())
                .filteredOn(document -> document.getId().startsWith("2026-2:map:college-departments:"))
                .extracting(AcademicGuideCatalog.AcademicGuideDocument::getContent)
                .anySatisfy(content -> assertThat(content).contains("문헌정보학전공", "인문대학", "문정"))
                .anySatisfy(content -> assertThat(content).contains("응용통계학전공", "사회과학대학", "응통"))
                .anySatisfy(content -> assertThat(content).contains("디지털미디어학부", "미디어·휴먼라이프대학", "디미"))
                .anySatisfy(content -> assertThat(content).contains("컴퓨터공학전공", "반도체·ICT대학", "컴공"));
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
