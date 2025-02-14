package nova.mjs.util.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nova.mjs.member.MemberDTO;
import nova.mjs.util.security.CustomUserDetailsService;
import nova.mjs.util.jwt.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class FormLoginJwtFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            //로그인 정보를 읽음
            MemberDTO.LoginRequestDTO loginRequest = new ObjectMapper().readValue(request.getInputStream(), MemberDTO.LoginRequestDTO.class);
            //authentication 객체 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()); //비밀번호가 없지안나?
            
            //인증 수행(userDetailsService 이용)
            //검증 성공 -> 인증된 authentication 객체 반환
            return authenticationManager.authenticate(authenticationToken); 
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 오류", e);
        }
    }

    //인증 성공시 jwt 생성
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String email = authResult.getName(); //인증된 사용자의 email - security에서 기본적으로 principal 반환
        String token = jwtUtil.generateAccessToken(email, "ROLE_USER"); //email, role_user 포함하는 jwt 생성

        response.setContentType("application/json"); //응답을 json 형식으로 설정
        response.getWriter().write("{\"token\": \"" + token + "\"}"); //전송 형식
    }
}