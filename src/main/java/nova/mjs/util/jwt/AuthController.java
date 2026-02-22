package nova.mjs.util.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.AuthDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    /**
     * Refresh Token 기반 Access Token 재발급.
     *
     * 계약(Contract):
     * - refreshToken은 HttpOnly Cookie로 전달된다.
     * - Authorization 헤더에 Access Token이 없더라도 호출 가능해야 한다. (permitAll)
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthDTO.TokenResponseDTO>> reissueAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        log.info("토큰 재발급 요청");

        // 쿠키가 없거나 비어있으면, JWT 파싱 전에 즉시 실패 처리 (형식 에러 방지)
        if (refreshToken == null || refreshToken.isBlank()) {
            // 너희 프로젝트 예외/응답 규격에 맞춰 예외로 던져도 됨
            throw new IllegalArgumentException("refreshToken cookie is missing");
        }

        AuthDTO.TokenResponseDTO newAccessToken = jwtUtil.reissueToken(refreshToken);

        log.info("새로운 Access Token 발급 완료");
        return ResponseEntity.ok(ApiResponse.success(newAccessToken));
    }
}