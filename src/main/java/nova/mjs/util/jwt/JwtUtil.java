package nova.mjs.util.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.jwt.exception.*;
import nova.mjs.util.jwt.exception.JwtException;
import nova.mjs.util.security.AuthDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import io.jsonwebtoken.security.SignatureException;

@Component
@Slf4j
public class JwtUtil {
    private final Key secretKey;
    private final long accessTokenExpiration = 60 * 1000L * 30 * 48 * 3000;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey, //yml에서 로드
            //@Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secretKey = decodeSecretKey(secretKey); //인코딩 오류를 막으면서 암호화 적용
        //this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    private Key decodeSecretKey(String encodedKey){
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT Access Token 생성 */
    public String generateAccessToken(UUID uuid, String email, String role) {
        return Jwts.builder()
                .setSubject(uuid.toString())  // 사용자 uuid
                .claim("email", email)
                .claim("role", role)  // 사용자 역할
                .setIssuedAt(new Date())  // 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))  // 만료 시간 - 밀리세컨드
                //currentTimeMillis()는 현재 시스템 시간, getTime()은 객체의 시간 반환 -> 객체의 시간이 더 맞으려나?
                //후자는 Date 객체를 생성하므로 조금 더 느림 / 특정 시간 값이 있으면 적절함 / 성능은 전자가 더 좋음
                .signWith(secretKey, SignatureAlgorithm.HS256)  // HMAC-SHA256 서명
                .compact(); //최종적으로 토큰 생성 -> jwt를 문자열로 반환하여 헤더에 포함되도록 함 - 문자열로 직렬화
    }

    //JWT Refresh Token 생성
    public String generateRefreshToken(UUID uuid, String email) {
        return Jwts.builder()
                .setSubject(uuid.toString())  // 사용자 uuid
                .claim("email", email)
                .setId(UUID.randomUUID().toString())  // JWT 고유 식별자 (JTI) - 블랙리스트 관리 가능
                .setIssuedAt(new Date())  // 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))  // 만료 시간
                .claim("type", "RefreshToken")  // Refresh Token 구분
                .signWith(secretKey, SignatureAlgorithm.HS256)  // HMAC-SHA256 서명
                .compact();
    }

    //토큰에서 사용자 uuid 추출
    public UUID getUserIdFromToken(String token) {
        String uuidString = getClaimFromToken(token, Claims::getSubject);
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }

    // 토큰에서 사용자 email 추출
    public String getEmailFromToken(String token){
        return getClaimFromToken(token, claims -> claims.get("email", String.class));
    }

    public String getRoleFromToken(String token) { //"role을 추출하는 메서드
        Claims claims = getAllClaimsFromToken(token);
        if (claims != null) {
            Object roleClaim = claims.get("role");
            return roleClaim != null ? roleClaim.toString() : null;
        }
        return null;
    }

    //토큰에서 특정 Claim 추출 - 필요한 부분만 추출
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims != null ? claimsResolver.apply(claims) : null; //claim을 추출하기 위함 : claimResolver.apply(claims) -> claims.getSubject()
    }

    //JWT 검증 및 Claim 정보 추출
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

    //토큰 형식 검증
    public String extractToken(String bearerToken){
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")){
            log.warn("[MJS] 유효하지 않은 JWT 형식입니다.");
            throw new InvalidTokenFormatException("유효하지 않은 JWT 형식입니다.");
        }
        return bearerToken.substring(7);
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("[MJS] JWT 토큰이 제공되지 않았습니다.");
            throw new TokenNotProvidedException("JWT 토큰이 제공되지 않았습니다.");
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token); //만료된 토큰이면, 여기서 예외 발생

            return true; // 토큰이 유효하면 true 반환

        } catch (ExpiredJwtException e) {
            log.warn("[MJS] JWT 토큰이 만료되었습니다: {}", e.getMessage());
            throw new JwtExpiredException(); //401
        } catch (MalformedJwtException e) {
            log.warn("[MJS] JWT 토큰이 변조되었거나 잘못된 형식입니다: {}", e.getMessage());
            throw new JwtMalformedException(); //401
        } catch (SignatureException e) {
            log.warn("[MJS] JWT 토큰 서명이 올바르지 않습니다: {}", e.getMessage());
            throw new JwtSignatureInvalidException(); //401
        } catch (UnsupportedJwtException e) {
            log.warn("[MJS] 지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
            throw new JwtUnsupportedException(); //401
        } catch (JwtException e) {
            log.warn("[MJS] 유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            throw new JwtInvalidException(); //401
        }

    }

    // JWT가 Refresh Token인지 확인
    public boolean isRefreshToken(String token) {
        String type = getClaimFromToken(token, claims -> claims.get("type", String.class));
        return "RefreshToken".equals(type);
    }

    // Access Token 재발급
    // Access Token 재발급
    public AuthDTO.TokenResponseDTO reissueToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenNotProvidedException("Refresh Token이 제공되지 않았습니다.");
        }

        // 🔹 Bearer 강제 제거 (AccessToken과 정책 분리)
        String token = refreshToken.trim();

        // 혹시 모를 Bearer 접두어 방어적 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        validateToken(token);

        if (!isRefreshToken(token)) {
            throw new NotRefreshTokenException();
        }

        UUID uuid = getUserIdFromToken(token);
        String email = getEmailFromToken(token);
        String role = getClaimFromToken(token, claims -> claims.get("role", String.class));

        if (uuid == null || email == null) {
            throw new RefreshTokenParseFailedException(
                    "Refresh Token에서 사용자 정보를 추출할 수 없습니다."
            );
        }

        String newAccessToken = generateAccessToken(uuid, email, role);

        return AuthDTO.TokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .build();
    }
}
