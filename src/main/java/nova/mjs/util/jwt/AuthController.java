package nova.mjs.util.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.AuthDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    /**
     * Refresh Token 기반 Access Token 재발급 정책:
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

        // 쿠키 없음 = 인증 안된 상태
        if (refreshToken == null) {
            log.warn("Reissue 실패: refreshToken 쿠키 없음");
            throw new RuntimeException("REFRESH_TOKEN_MISSING");
        }

        // 검증 및 재발급 (내부에서 만료/위조 검사 수행)
        AuthDTO.TokenResponseDTO newAccessToken = jwtUtil.reissueToken(refreshToken);

        log.info("Access Token 재발급 성공");
        return ResponseEntity.ok(ApiResponse.success(newAccessToken));
    }
}
