package nova.mjs.config.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimeLoggerAspect {

    @Around("execution(* nova.mjs.domain..service..*(..)) && !@annotation(nova.mjs.config.aop.LogExecutionTime)")
    public Object logExecutionTimeByPackage(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("[ExecutionTime] {} → {} ms", joinPoint.getSignature().toShortString(), duration);
        }
    }

    @Around("@annotation(logExecutionTime)")
    public Object logExecutionTimeByAnnotation(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("[ET-Annotation] {} → {} ms {}", joinPoint.getSignature().toShortString(), duration, logExecutionTime.value());
        }
    }
}
