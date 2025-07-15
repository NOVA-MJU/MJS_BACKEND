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

    public static DepartmentSummaryDTO of(Department d) {
        return DepartmentSummaryDTO.builder()
                .departmentUuid(d.getDepartmentUuid())
                .departmentName(d.getDepartmentName())
                .studentCouncilName(d.getStudentCouncilName())
                .studentCouncilLogo(d.getStudentCouncilLogo())
                .slogan(d.getSlogan())
                .college(d.getCollege())
                .build();
    }
}