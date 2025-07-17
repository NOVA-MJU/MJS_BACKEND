package nova.mjs.domain.department.DTO;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.member.entity.enumList.College;
import nova.mjs.domain.member.entity.enumList.DepartmentName;

import java.util.UUID;

@Getter
@Builder
public class DepartmentSummaryDTO {
    private UUID departmentUuid;
    private DepartmentName departmentName;
    private String studentCouncilName;
    private String studentCouncilLogo;
    private String slogan;
    private College college;

    public static DepartmentSummaryDTO fromEntity(Department department) {
        return DepartmentSummaryDTO.builder()
                .departmentUuid(department.getDepartmentUuid())
                .departmentName(department.getDepartmentName())
                .studentCouncilName(department.getAdmin().getName())
                .studentCouncilLogo(department.getAdmin().getProfileImageUrl())
                .slogan(department.getSlogan())
                .college(department.getCollege())
                .build();
    }
}