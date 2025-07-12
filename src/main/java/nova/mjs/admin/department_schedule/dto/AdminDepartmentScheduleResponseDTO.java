package nova.mjs.admin.department_schedule.dto;

import lombok.Builder;
import lombok.Data;
import nova.mjs.department.entity.DepartmentSchedule;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class AdminDepartmentScheduleResponseDTO {
    private UUID departmentScheduleUuid;
    private String title;
    private String content;
    private String colorCode;
    private LocalDate startDate;
    private LocalDate endDate;

    public static AdminDepartmentScheduleResponseDTO fromEntity(DepartmentSchedule schedule) {
        return AdminDepartmentScheduleResponseDTO.builder()
                .departmentScheduleUuid(schedule.getDepartmentScheduleUuid())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .colorCode(schedule.getColorCode())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .build();
    }
}
