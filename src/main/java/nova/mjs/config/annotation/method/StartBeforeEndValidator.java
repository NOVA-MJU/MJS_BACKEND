package nova.mjs.config.annotation.method;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleRequestDTO;
import nova.mjs.config.annotation.ValidEndDateRange;

import java.time.LocalDate;

public class StartBeforeEndValidator implements ConstraintValidator<ValidEndDateRange, AdminDepartmentScheduleRequestDTO> {

    @Override
    public boolean isValid(AdminDepartmentScheduleRequestDTO dto, ConstraintValidatorContext context) {
        LocalDate start = dto.getStartDate();
        LocalDate end = dto.getEndDate();

        // null 값은 @NotNull에서 처리하므로 여기서는 검사 제외
        if (start == null || end == null) {
            return true;
        }

        if (start.isAfter(end)) {
            // ✅ 기본 메시지 설정이 되어 있어도 아래 코드를 통해 명시적으로 메시지를 설정 가능
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("시작일은 종료일보다 늦을 수 없습니다.")
                    .addPropertyNode("endDate") // 오류가 어디에 해당하는지 지정 가능
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
