package nova.mjs.domain.thingo.department.directory;

import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentDirectoryCatalogTest {

    private final DepartmentDirectoryCatalog catalog = new DepartmentDirectoryCatalog();

    @Test
    void loadsSevenCollegeRootsAndAllDepartmentEntries() {
        assertThat(catalog.entries()).hasSize(53);
        assertThat(catalog.entries().stream().filter(entry -> entry.departmentName() == null))
                .hasSize(7);
    }

    @Test
    void resolvesDepartmentDespiteSpacesAndMiddleDots() {
        DepartmentDirectoryCatalog.Entry entry = catalog.resolve(
                "인공지능 · 소프트웨어융합대학 데이터 사이언스 전공 소개").orElseThrow();

        assertThat(entry.college()).isEqualTo(College.AI_SOFTWARE);
        assertThat(entry.departmentName()).isEqualTo(DepartmentName.DATA_SCIENCE);
    }

    @Test
    void resolvesDepartmentWhenDepartmentSuffixIsOmitted() {
        DepartmentDirectoryCatalog.Entry entry = catalog.resolve("데이터사이언스").orElseThrow();

        assertThat(entry.college()).isEqualTo(College.AI_SOFTWARE);
        assertThat(entry.departmentName()).isEqualTo(DepartmentName.DATA_SCIENCE);
    }

    @Test
    void resolvesApplicationSoftwareAliasesToTheSameDepartment() {
        assertThat(catalog.resolve("응소 소개").orElseThrow().departmentName())
                .isEqualTo(DepartmentName.APPLICATION_SOFTWARE);
        assertThat(catalog.resolve("응용소프트웨어학과 홈페이지").orElseThrow().departmentName())
                .isEqualTo(DepartmentName.APPLICATION_SOFTWARE);
        assertThat(catalog.resolve("응용소프트웨어전공 소개").orElseThrow().departmentName())
                .isEqualTo(DepartmentName.APPLICATION_SOFTWARE);
    }

    @Test
    void resolvesManagementInformationSystemsAlias() {
        assertThat(catalog.resolve("경정 소개").orElseThrow().departmentName())
                .isEqualTo(DepartmentName.MANAGEMENT_INFORMATION_SYSTEMS);
    }

    @Test
    void publicServiceSchoolAndAdministrationMajorUseDifferentCanonicalIds() {
        assertThat(catalog.resolve("공공인재학부 홈페이지").orElseThrow().departmentName())
                .isEqualTo(DepartmentName.SCHOOL_OF_PUBLIC_SERVICE);
        assertThat(catalog.resolve("행정학전공 홈페이지").orElseThrow().departmentName())
                .isEqualTo(DepartmentName.PUBLIC_ADMINISTRATION);
    }

    @Test
    void resolvesParenthesizedDepartmentName() {
        assertThat(catalog.resolve("자율전공학부 인문 전화번호").orElseThrow().departmentName())
                .isEqualTo(DepartmentName.FREE_MAJOR);
    }
}
