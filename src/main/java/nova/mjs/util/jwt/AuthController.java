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
     * Access Token 재발급
     *
     * 정책:
     * - refreshToken은 HttpOnly 쿠키로만 전달됨
     * - 없거나 검증 실패 시 예외 발생 → GlobalExceptionHandler에서 401 처리
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthDTO.TokenResponseDTO>> reissueAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {

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