package nova.mjs.domain.department.dto;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.member.entity.enumList.College;
import nova.mjs.domain.member.entity.enumList.DepartmentName;

import java.util.UUID;

@Getter
@Builder
public class DepartmentInfoDTO {
    private UUID departmentUuid;
    private DepartmentName departmentName;
    private String studentCouncilName;
    private String studentCouncilLogo;
    private String studentCouncilContactEmail;
    private String slogan;
    private String description;
    private String instagramUrl;
    private String homepageUrl;
    private College college;

    public static DepartmentInfoDTO fromEntity(Department department) {
        return DepartmentInfoDTO.builder()
                .departmentUuid(department.getDepartmentUuid())
                .departmentName(department.getDepartmentName())
                .studentCouncilName(department.getAdmin().getName())
                .studentCouncilLogo(department.getAdmin().getProfileImageUrl())
                .slogan(department.getSlogan())
                .description(department.getDescription())
                .instagramUrl(department.getInstagramUrl())
                .homepageUrl(department.getHomepageUrl())
                .college(department.getCollege())
                .build();
    }
}
