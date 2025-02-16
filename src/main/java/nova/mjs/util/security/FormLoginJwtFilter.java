package nova.mjs.util.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.jwt.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class FormLoginJwtFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final FormLoginSuccessHandler successHandler;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("로그인 요청 메서드 : {}", request.getMethod());
        log.info("로그인 요청 content-type : {}", request.getContentType());


        if (!"application/json".equals(request.getContentType())){
            throw new AuthenticationServiceException("Json 형식이 아님");
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AuthDTO.LoginRequestDTO loginRequest = objectMapper.readValue(request.getInputStream(), AuthDTO.LoginRequestDTO.class);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            
            //인증 수행(userDetailsService 이용)
            //검증 성공 -> 인증된 authentication 객체 반환
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            log.info("인증 성공: {}", authentication.getName());

            return authentication;
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 오류", e);
        }
    }

    //인증 성공시 jwt 생성
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("인증 성공 handler 실행");
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }
}