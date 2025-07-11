package nova.mjs.config.aop;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogExecutionTime {
    String value() default "";  // 실행시간 설명 (옵션)
}
