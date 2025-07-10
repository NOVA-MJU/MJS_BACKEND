package nova.mjs.admin.department_schedule.dto;

import lombok.Builder;
import lombok.Data;
import nova.mjs.admin.department_schedule.entity.DepartmentSchedule;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class DepartmentScheduleResponseDTO {
    private UUID uuid;
    private String title;
    private String content;
    private String colorCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String department;

    public static DepartmentScheduleResponseDTO fromEntity(DepartmentSchedule schedule) {
        return DepartmentScheduleResponseDTO.builder()
                .uuid(schedule.getUuid())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .colorCode(schedule.getColorCode())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .department(schedule.getAdmin().getDepartment())
                .build();
    }
}
