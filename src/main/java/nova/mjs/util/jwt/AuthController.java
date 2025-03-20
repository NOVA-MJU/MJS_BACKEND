package nova.mjs.util.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.AuthDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthDTO.TokenResponseDTO>> reissueAccessToken(
            @RequestHeader("Authorization") String refreshToken) {
        log.info("토큰 재발급 요청");

        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            log.warn("유효하지 않은 Refresh Token 형식");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.success(null));
        }

        String token = refreshToken.substring(7);
        Optional<String> newAccessToken = jwtUtil.reissueToken(token);

        return newAccessToken.map(accessToken -> {
            log.info("새로운 Access Token 발급 완료");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(new AuthDTO.TokenResponseDTO(accessToken)));
        }).orElseGet(() -> {
            log.warn("Refresh Token이 유효하지 않음");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.success(null)); // 에러 메시지 없이 null 반환
        });
    }
}
