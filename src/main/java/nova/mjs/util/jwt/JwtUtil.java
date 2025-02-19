package nova.mjs.util.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret, //yml에서 로드
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); //인코딩 오류를 막으면서 암호화 적용
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // JWT Access Token 생성 */
    public String generateAccessToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)  // 사용자 ID
                .claim("role", role)  // 사용자 역할
                .setIssuedAt(new Date())  // 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))  // 만료 시간 - 밀리세컨드
                //currentTimeMillis()는 현재 시스템 시간, getTime()은 객체의 시간 반환 -> 객체의 시간이 더 맞으려나?
                //후자는 Date 객체를 생성하므로 조금 더 느림 / 특정 시간 값이 있으면 적절함 / 성능은 전자가 더 좋음
                .signWith(secretKey, SignatureAlgorithm.HS256)  // HMAC-SHA256 서명
                .compact(); //최종적으로 토큰 생성 -> jwt를 문자열로 반환하여 헤더에 포함되도록 함 - 문자열로 직렬화
    }

    //JWT Refresh Token 생성
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)  // 사용자 ID
                .setId(UUID.randomUUID().toString())  // JWT 고유 식별자 (JTI) - 블랙리스트 관리 가능
                .setIssuedAt(new Date())  // 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))  // 만료 시간
                .claim("type", "RefreshToken")  // Refresh Token 구분
                .signWith(secretKey, SignatureAlgorithm.HS256)  // HMAC-SHA256 서명
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public String getEmailFromToken(String token){
        return getClaimFromToken(token, Claims::getSubject);
    } //이미 아래에서 claim을 추출하는 메서드가 있으므로 필요 없을 수도 있어보임

    public String getRoleFromToken(String token) { //"role을 추출하는 메서드
        Claims claims = getAllClaimsFromToken(token);
        if (claims != null) {
            Object roleClaim = claims.get("role");
            return roleClaim != null ? roleClaim.toString() : null;
        }
        return null;
    }

    /** 토큰에서 특정 Claim 추출 */ //필요한 부분만 추출
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims != null ? claimsResolver.apply(claims) : null; //claim을 추출하기 위함 : claimResolver.apply(claims) -> claims.getSubject()
    }

    /** JWT 검증 및 Claim 정보 추출 */
    private Claims getAllClaimsFromToken(String token) {
        try{
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody(); //전체 claims 반환 - 모든 claims 데이터를 claims 객체로 가져옴 -> 한 번에 접근 가능
        } catch (JwtException e){
            return null;
        }
    }

    /** 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (claims == null){
            log.error("JWT 파싱 실패 : 올바른 토큰이 아닙니다.");
            return false;
        }
        boolean isExpired = claims.getExpiration().before(new Date());

        if (isExpired){ //만료 여부 확인
            log.warn("JWT 토큰이 만료되었습니다.");
        }
        return !isExpired;
    }

    
    // JWT가 Refresh Token인지 확인
    public boolean isRefreshToken(String token) {
        String type = getClaimFromToken(token, claims -> claims.get("type", String.class));
        return "RefreshToken".equals(type);
    }

    // JWT가 만료되었는지 확인
    public boolean isTokenExpired(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims == null || claims.getExpiration().before(new Date());
    }

    // Access Token 재발급
    public Optional<String> reissueToken(String refreshToken) {
        // 1. Refresh Token 유효성 검사
        if (!validateToken(refreshToken) || !isRefreshToken(refreshToken)) {
            return Optional.empty(); // 유효하지 않은 경우 빈 값 반환
        }

        // 2. Refresh Token에서 사용자 ID 및 역할(Role) 추출
        String email = getEmailFromToken(refreshToken);
        String role = getClaimFromToken(refreshToken, claims -> claims.get("role", String.class));

        // 3. 새로운 Access Token 생성 후 반환
        return Optional.of(generateAccessToken(email, role));
    }
}
