// src/main/java/nova/mjs/department/DTO/DepartmentSummaryDTO.java
package nova.mjs.department.DTO;

import lombok.Builder;
import lombok.Getter;
import nova.mjs.department.entity.enumList.College;

import java.util.UUID;

@Getter
@Builder
public class DepartmentSummaryDTO {
    private UUID departmentUuid;
    private String departmentName;
    private String studentCouncilName;
    private String studentCouncilLogo;
    private String slogan;
    private College college;

    public static DepartmentSummaryDTO of(nova.mjs.department.entity.Department d) {
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
