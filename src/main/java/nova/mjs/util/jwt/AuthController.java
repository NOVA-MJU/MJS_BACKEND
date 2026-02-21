package nova.mjs.util.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.AuthDTO;
import org.springframework.http.HttpStatus;
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
     * 동작 방식:
     * - HttpOnly 쿠키에 저장된 refreshToken 사용
     * - 프론트는 토큰을 직접 읽지 않는다
     * - 브라우저가 자동으로 쿠키를 첨부한다
     *
     * 실패 케이스:
     * - 쿠키 없음 → 로그인 안된 상태 → 401
     * - 만료/위조/블랙리스트 → 401
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthDTO.TokenResponseDTO>> reissueAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {

        // 1. 쿠키 없음 → 인증 안된 상태
        if (refreshToken == null) {
            log.warn("Reissue 실패: refreshToken 쿠키 없음");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("REFRESH_TOKEN_MISSING"));
        }

        try {
            // 2. refresh 검증 및 새 access 발급
            AuthDTO.TokenResponseDTO newAccessToken = jwtUtil.reissueToken(refreshToken);

            log.info("Access Token 재발급 성공");
            return ResponseEntity.ok(ApiResponse.success(newAccessToken));

        } catch (Exception e) {
            // 3. 만료 / 위조 / 블랙리스트
            log.warn("Reissue 실패: {}", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("INVALID_REFRESH_TOKEN"));
        }
    }
}