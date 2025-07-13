package nova.mjs.util.security;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.jwt.AccessTokenBlacklistRepository;
import nova.mjs.util.jwt.JwtUtil;
import nova.mjs.util.jwt.TokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           AuthenticationManager authenticationManager,
                                           JwtUtil jwtUtil,
                                           FormLoginSuccessHandler successHandler,
                                           AccessTokenBlacklistRepository accessTokenBlacklistRepository, TokenRepository tokenRepository) throws Exception {

        FormLoginJwtFilter formLoginJwtFilter = new FormLoginJwtFilter(authenticationManager, jwtUtil, successHandler);
        //formLoginJwtFilter.setAuthenticationManager(authenticationManager);
        formLoginJwtFilter.setFilterProcessesUrl("/api/v1/auth/login"); // 커스텀 로그인

        //security 내부에 formlogin() 기능을 통해 내부적으로 폼 로그인 수행
        //UsernamePasswordAuthenticationFilter를 활용해 AuthenticationManager로 인증 수행
        //handler -> 인증 성공 시, 두 토큰 생성


        httpSecurity
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin) // H2 콘솔이 프레임으로 로드 가능하도록 설정
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //JWT 사용 -> 세션 미사용
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/auth/logout").authenticated()
                        .requestMatchers("/**").permitAll() //이걸 로그인으로 해놔서 config가 가로채감(주의)
                        .anyRequest().authenticated()) //나머지 요청은 인증 필요
                .addFilterAt(formLoginJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginProcessingUrl("/api/v1/auth/login") //로그인 url 지정
                        .successHandler(new FormLoginSuccessHandler(jwtUtil)) //로그인 성공 핸들러
                        .permitAll()) //로그인 실패 핸들러는 없어도 무방 -> 내부적으로 실패 로직으로 401을 뱈음
                .addFilterAfter(new JwtAuthenticationFilter(jwtUtil, customUserDetailsService, accessTokenBlacklistRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new LogoutFilter(jwtUtil, tokenRepository, accessTokenBlacklistRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.error("접근 거부 - 403 Forbidden! 요청 URL: {}", request.getRequestURI());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        }));


        return httpSecurity.build();
    }

   @Bean
   public CorsConfigurationSource corsConfigurationSource() {
       CorsConfiguration configuration = new CorsConfiguration();
       // 모든 출처 허용
       configuration.addAllowedOriginPattern("*");
       configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
       configuration.setAllowedHeaders(Arrays.asList("*"));
       configuration.setAllowCredentials(true);
       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", configuration);
       return source;
   }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
