package nova.mjs.config.aop;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AopConfig {
    /**
     *  AOP(Aspect-Oriented Programming) 설정 클래스
     *
     * Spring AOP를 사용하려면 반드시 @EnableAspectJAutoProxy 애노테이션을 통해 AOP 기능을 활성화해야 한다.
     *
     * 기본적으로 Spring은 "JDK 동적 프록시" 방식을 사용하는데, 이 경우 인터페이스에 선언된 메서드만 AOP 대상이 된다.
     * 그러나 우리 프로젝트에서는 다음과 같은 상황이 혼합되어 있어 문제가 발생할 수 있다:
     *
     * 1. 인터페이스가 존재하는 서비스 클래스 (예: EmailContactService → EmailContactServiceImpl)
     *    → 기본 JDK Proxy 사용 시, 구현체에 정의된 AOP가 적용되지 않는 문제가 발생함
     *
     * 2. 인터페이스 없이 구현체만 존재하는 클래스 (예: EmailService)
     *    → 자동으로 CGLIB Proxy가 적용되어 AOP가 정상 동작함
     *
     * 이 문제를 해결하기 위해 proxyTargetClass=true 옵션을 사용하여
     * **항상 CGLIB 기반의 클래스 프록시를 사용하도록 강제한다.**
     *
     * 이렇게 하면 인터페이스 유무와 무관하게 모든 클래스에 대해 AOP가 정상 작동하며,
     * 특히 구현체 클래스에 직접 작성한 @Aspect(Around) 포인트컷들이 잘 적용된다.
     *
     */
}
