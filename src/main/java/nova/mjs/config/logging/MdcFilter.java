package nova.mjs.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 🌐 MdcFilter
 * - 요청마다 고유한 Trace ID를 생성하여 MDC(Mapped Diagnostic Context)에 저장하는 필터
 * - 로그에 Trace ID가 자동으로 포함되어 요청별 로그 추적이 가능하게 해줌
 */
@Component
public class MdcFilter extends OncePerRequestFilter {

    // MDC에 저장할 키 이름 (로그 패턴에서 %X{TRACE_ID}로 사용됨)
    private static final String TRACE_ID = "TRACE_ID";

    /**
     * 요청마다 실행되는 필터 메서드
     * - UUID 기반 Trace ID 생성 및 MDC에 저장
     * - 요청 처리 후 MDC 클리어 (메모리 누수 방지)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 요청별 고유한 Trace ID 생성 (8자리)
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        // MDC에 Trace ID 저장 → 로그 패턴에서 자동 출력
        MDC.put(TRACE_ID, traceId);

        try {
            // 다음 필터/컨트롤러로 요청 전달
            filterChain.doFilter(request, response);
        } finally {
            // 요청 처리 후 MDC 비우기 (반드시)
            MDC.clear();
        }
    }
}
