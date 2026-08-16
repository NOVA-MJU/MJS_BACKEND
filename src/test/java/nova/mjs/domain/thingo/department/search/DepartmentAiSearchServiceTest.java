package nova.mjs.domain.thingo.department.search;

import nova.mjs.domain.thingo.department.directory.DepartmentDirectoryCatalog;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentProfile;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentProfileRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentScheduleRepository;
import nova.mjs.domain.thingo.search.academic.AcademicGuideCatalog;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DepartmentAiSearchServiceTest {

    @Test
    void basicIntroductionReturnsTypedProfileAndOfficialSourceWithoutChangingLegacyApi() {
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        DepartmentProfileRepository profileRepository = mock(DepartmentProfileRepository.class);
        Department department = Department.builder()
                .college(College.AI_SOFTWARE)
                .departmentName(DepartmentName.DATA_SCIENCE)
                .academicOfficePhone("02-300-0643")
                .instagramUrl("https://www.instagram.com/mju_sw/")
                .homepageUrl("https://www.mju.ac.kr/software/index.do")
                .build();
        DepartmentProfile profile = DepartmentProfile.builder()
                .department(department)
                .introduction("데이터 분석과 소프트웨어 개발 역량을 함께 기르는 전공입니다.")
                .introductionSourceUrl("https://www.mju.ac.kr/software/9783/subview.do")
                .collectionStatus("COMPLETE")
                .verifiedAt(LocalDateTime.of(2026, 8, 16, 12, 0))
                .build();
        when(departmentRepository.findByCollegeAndDepartmentName(
                College.AI_SOFTWARE, DepartmentName.DATA_SCIENCE)).thenReturn(Optional.of(department));
        when(profileRepository.findByDepartment(department)).thenReturn(Optional.of(profile));

        DepartmentAiSearchService service = new DepartmentAiSearchService(
                new DepartmentDirectoryCatalog(),
                departmentRepository,
                profileRepository,
                mock(DepartmentNoticeRepository.class),
                mock(DepartmentScheduleRepository.class),
                mock(AcademicGuideCatalog.class));

        DepartmentAiSearchDTO.Response response = service.search(
                "데이터 사이언스 전공 소개", DepartmentAiSearchDTO.Category.AUTO);

        assertThat(response.getCategory()).isEqualTo(DepartmentAiSearchDTO.Category.BASIC);
        assertThat(response.getEntity().getId())
                .isEqualTo("DEPARTMENT:AI_SOFTWARE:DATA_SCIENCE");
        assertThat(response.getBlocks())
                .extracting(DepartmentAiSearchDTO.Block::getType)
                .containsExactly(
                        DepartmentAiSearchDTO.BlockType.PROFILE_CARD,
                        DepartmentAiSearchDTO.BlockType.TEXT_ANSWER,
                        DepartmentAiSearchDTO.BlockType.SOURCE_LIST);
        assertThat(response.getBlocks().get(0).getProfile().getAcademicOfficePhone())
                .isEqualTo("02-300-0643");
    }
}
