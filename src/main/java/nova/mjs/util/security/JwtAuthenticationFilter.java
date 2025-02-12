package nova.mjs.util.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    //GenericFilterBean vs. OncePerRequestFilter
    //GenericFilterBean : 멀티 실행 가능 -> doFilter()를 직접 구현 / 요청이 올때마다 실행 / 한 요청에서 여러번 실행될 가능성 있음
    //한 요청당 한 번만 실행됨을 보장 -> doFilterInternal() / servlet request와 response가 가능
    //JWT 인증, CORS 설정 : OncePerRequestFilter
    //트랜잭션 로깅, 요청 감시(Audit) : GenericFilterBean

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 요청에서 JWT 추출
            String token = extractTokenFromRequest(request);

            // 2. 블랙리스트 체크 (추후 Redis 블랙리스트 구현과 연결)
//            if (token != null && jwtUtil.isTokenBlacklisted(token)) {
//                throw new SecurityException("로그아웃된 토큰입니다.");
//            }

            // 3. 토큰 검증 및 사용자 인증
            if (token != null && jwtUtil.validateToken(token)) {
                Authentication authentication = authenticate(token);
                
                if (SecurityContextHolder.getContext().getAuthentication() == null){
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
//            log.error("예상치 못한 예외 발생: {}", e.getMessage());
//            setErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류 발생");
//            return;
        }

        // 4. 정상적인 요청이면 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    //요청에서 JWT를 추출
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    //JWT를 검증하고 인증 객체를 생성하여 SecurityContext에 저장
    private Authentication authenticate(String token) {
        String userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        //UserDetails userDetails = userDetailsService.loadUserByUsername(userId); // 사용자 정보 조회

        UserPrincipal userPrincipal = new UserPrincipal(userId, role);

        return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    //HTTP 응답으로 에러 메시지를 반환하는 메서드
//    private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
//        response.setStatus(status);
//        response.setContentType("application/json; charset=UTF-8");
//        response.getWriter().write(ApiResponse.fail(message).toJson()); // ApiResponse를 JSON으로 변환하여 반환
//    }
}
