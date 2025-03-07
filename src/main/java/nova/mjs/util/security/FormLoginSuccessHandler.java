package nova.mjs.util.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.jwt.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {//로그인 성공 시 jwt 생성, 응답 설정

    //로그인 성공 시 자동으로 호출되는 handler
    //filter 부분에서 인증 성공 후 jwt 생성하고 응답하는 역할을 구현했었음
    //filter만 사용 - 필터에서 인증 flow 관리하므로 security 인증 절차에 통합, 성공시 contextHolder에 자동 저장
    //대신 시간이나 로그를 기록하지 못함
    //handler - 성공 이벤트에서 실행, 인증과 인증 성공 후 작업이 분리 -> 재사용 가능 / 기록 저장, 보안 로깅 추가 용이

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("로그인 성공 {}", authentication.getName());

        //인증된 사용자 정보 가져오기
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID uuid = principal.getUuid();
        String email = principal.getEmail(); //SecurityContext에서 username(email) 가져오기
        //String email = authentication.getName(); 이것도 맞을 수 있음. principal에서 getUserName을 email로 반환하기 때문
        String role = "ROLE_USER";


        //JWT 생성
        String accessToken = jwtUtil.generateAccessToken(uuid, email, role);
        String refreshToken = jwtUtil.generateRefreshToken(uuid, email);

        log.info("발급된 Access Token: {}", accessToken);
        log.info("발급된 Refresh Token: {}", refreshToken);

        //응답 json 생성 - 데이터 준비
        AuthDTO.LoginResponseDTO responseData = AuthDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        // 실제 응답이 정상적으로 반환되는지 확인
        log.info("로그인 응답 데이터: {}", responseData);

        //json 응답 설정
        response.setContentType("application/json"); //response가 json 형식임을 명시
        response.getWriter().write(objectMapper.writeValueAsString(responseData)); //Map 객체를 json 문자열로 변환 후 응답

        log.info("JWT 응답 전송 완료");
    }
}
