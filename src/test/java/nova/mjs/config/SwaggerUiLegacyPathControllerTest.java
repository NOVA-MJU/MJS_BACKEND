package nova.mjs.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerUiLegacyPathControllerTest {

    private final SwaggerUiLegacyPathController controller =
            new SwaggerUiLegacyPathController();

    @Test
    void forwardsLegacySwaggerUiPathWithoutChangingPublicUrl() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/swagger-ui/index.html");

        String view = controller.forwardLegacySwaggerUi(request);

        assertThat(view).isEqualTo("forward:/swagger-ui/index.html");
    }

    @Test
    void forwardsLegacySwaggerDocsPath() {
        assertThat(controller.forwardLegacySwaggerDocs())
                .isEqualTo("forward:/docs");
    }
}
