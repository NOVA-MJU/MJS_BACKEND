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
            String token = resolveToken(request);

            // 2. 블랙리스트 체크 (추후 Redis 블랙리스트 구현과 연결)
//            if (token != null && jwtUtil.isTokenBlacklisted(token)) {
//                throw new SecurityException("로그아웃된 토큰입니다.");
//            }

            // 3. 토큰 검증 및 사용자 인증
            if (token != null && jwtUtil.validateToken(token)) {
                authenticateUser(token);
            }
        } catch (SecurityException e) {
//            log.error("보안 예외 발생: {}", e.getMessage());
//            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "인증 실패: " + e.getMessage());
//            return;
        } catch (Exception e) {
//            log.error("예상치 못한 예외 발생: {}", e.getMessage());
//            setErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류 발생");
//            return;
        }

        // 4. 정상적인 요청이면 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    //요청에서 JWT를 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    //JWT를 검증하고 인증 객체를 생성하여 SecurityContext에 저장
    private void authenticateUser(String token) {
        String userId = jwtUtil.getUserIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId); // 사용자 정보 조회

        //인증 객체 생성 - 사용자 권한을 SecurityContext에 설정
        //getAuthorities() : 사용자의 권한 목록
        //jwt에서는 password가 필요 없으므로 null 사용
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        //spring security에 인증된 사용자 정보 저장
        //ContextHolder는 security에서 현재 요청의 보안 컨텍스트를 저장하는 공간 - 저장되면 인증된 사용자로 간주
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    //HTTP 응답으로 에러 메시지를 반환하는 메서드
//    private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
//        response.setStatus(status);
//        response.setContentType("application/json; charset=UTF-8");
//        response.getWriter().write(ApiResponse.fail(message).toJson()); // ApiResponse를 JSON으로 변환하여 반환
//    }
}
