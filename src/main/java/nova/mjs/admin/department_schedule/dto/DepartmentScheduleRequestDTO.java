package nova.mjs.admin.department_schedule.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DepartmentScheduleRequestDTO {
    private String title;
    private String content;
    private String colorCode;
    private LocalDate startDate;
    private LocalDate endDate;
}
