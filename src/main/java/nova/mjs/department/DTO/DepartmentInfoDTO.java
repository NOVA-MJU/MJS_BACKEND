package nova.mjs.department.DTO;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.department.entity.Department;
import nova.mjs.department.entity.enumList.College;

import java.util.UUID;

@Getter
@Builder
public class DepartmentInfoDTO {
    private UUID departmentUuid;
    private String departmentName;
    private String studentCouncilName;
    private String studentCouncilLogo;
    private String slogan;
    private String description;
    private String instagramUrl;
    private String homepageUrl;
    private College college;

    public static DepartmentInfoDTO fromDepartmentEntity(Department department) {
        return DepartmentInfoDTO.builder()
                .departmentUuid(department.getDepartmentUuid())
                .departmentName(department.getDepartmentName())
                .studentCouncilName(department.getStudentCouncilName())
                .studentCouncilLogo(department.getStudentCouncilLogo())
                .slogan(department.getSlogan())
                .description(department.getDescription())
                .instagramUrl(department.getInstagramUrl())
                .homepageUrl(department.getHomepageUrl())
                .college(department.getCollege())
                .build();
    }
}
