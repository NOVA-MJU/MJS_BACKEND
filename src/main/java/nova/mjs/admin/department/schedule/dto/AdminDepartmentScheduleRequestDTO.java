package nova.mjs.admin.department.schedule.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import nova.mjs.config.annotation.ValidEndDateRange;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidEndDateRange
public class AdminDepartmentScheduleRequestDTO {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private String content;

    private String colorCode;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    private LocalDate endDate;
}

