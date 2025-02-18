package nova.mjs.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "openWeatherMapClient")
    public WebClient openWeatherMapClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.openweathermap.org")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
