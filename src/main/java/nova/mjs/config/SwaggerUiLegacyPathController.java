package nova.mjs.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 기존에 외부에 공개한 Swagger UI 주소를 유지한다.
 *
 * springdoc 정적 리소스는 /swagger-ui/** 에 두고,
 * /api/swagger-ui/** 요청만 서버 내부에서 같은 리소스로 포워딩한다.
 * 브라우저 주소는 기존 URL 그대로 유지된다.
 */
@Controller
public class SwaggerUiLegacyPathController {

    private static final String LEGACY_PREFIX = "/api";

    @GetMapping({"/api/swagger-ui", "/api/swagger-ui/**"})
    public String forwardLegacySwaggerUi(HttpServletRequest request) {
        return "forward:" + request.getRequestURI().substring(LEGACY_PREFIX.length());
    }

    @GetMapping("/api/docs")
    public String forwardLegacySwaggerDocs() {
        return "forward:/docs";
    }
}
