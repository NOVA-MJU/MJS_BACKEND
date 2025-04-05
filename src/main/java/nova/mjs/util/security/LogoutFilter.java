package nova.mjs.util.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.exception.ErrorResponse;
import nova.mjs.util.jwt.AccessTokenBlacklist;
import nova.mjs.util.jwt.AccessTokenBlacklistRepository;
import nova.mjs.util.jwt.TokenRepository;
import nova.mjs.util.jwt.JwtUtil;
import nova.mjs.util.jwt.exception.InvalidTokenFormatException;
import nova.mjs.util.jwt.exception.JwtException;
import nova.mjs.util.jwt.exception.TokenNotProvidedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LogoutFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 로그아웃 요청이 아니면 다음 필터로 이동
        if (!request.getRequestURI().equals("/api/v1/auth/logout") || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("로그아웃 요청 감지");

        try {
            // 요청에서 Access Token 추출
            String accessToken = extractTokenFromRequest(request);
            jwtUtil.validateToken(accessToken);

            // Refresh Token 삭제
            String email = jwtUtil.getEmailFromToken(accessToken);

            // 블랙리스트에 Access Token 추가
            accessTokenBlacklistRepository.save(new AccessTokenBlacklist(accessToken));

            // Refresh Token을 블랙리스트에 추가하여 향후 사용 차단(존재하면 삭제)
            tokenRepository.findByEmail(email).ifPresent(tokenRepository::delete);

            log.info("사용자 {} 로그아웃 - Access Token 블랙리스트 추가 및 Refresh Token 삭제", email);

            // SecurityContext 초기화
            SecurityContextHolder.clearContext();

            //클라이언트가 로그아웃 인지하도록 응답 헤더 초기화
            response.setHeader("Authorization", "");
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write("{\"message\": \"로그아웃 완료\"}");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JwtException ex){
            log.warn("[MJS] JWT 예외 발생: {}", ex.getMessage());

            ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getMessage());
            response.setStatus(ex.getStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return;
        } catch (Exception e){
            log.error("[MJS] 예기치 않은 예외 발생", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\": \"서버 내부 오류가 발생했습니다.\"}");

        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null){
            log.warn("Authorization 헤더가 존재하지 않습니다.");
            throw new TokenNotProvidedException();
        }
        if (!bearerToken.startsWith("Bearer ")) {
            log.warn("Authorizaion 헤더 형식이 올바르지 않습니다 : {}", bearerToken);
            throw new InvalidTokenFormatException();
        }
        return bearerToken.substring(7);
    }
}
