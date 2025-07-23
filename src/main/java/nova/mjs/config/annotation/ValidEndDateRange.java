package nova.mjs.config.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import nova.mjs.config.annotation.method.StartBeforeEndValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StartBeforeEndValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEndDateRange {
    String message() default "시작일은 종료일보다 늦을 수 없습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
