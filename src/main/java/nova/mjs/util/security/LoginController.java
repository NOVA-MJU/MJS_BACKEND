package nova.mjs.util.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.member.Member;
import nova.mjs.member.MemberDTO;
import nova.mjs.member.MemberService;
import nova.mjs.util.jwt.JwtUtil;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    //로그인 방식 : controller vs. formloginjwtfilter
    //controller - @RequestBody를 이용해 인증 / security의 흐름을 따르지 않으므로 보안 약함
    //컨트롤러에서 인증 수행하고 간단한 구현이 되지만, SecurityContext에 인증 정보가 자동 저장되지 않고,
    //jwt 생성 및 관리가 분리될 수 있음
    //formloginjwtfilter - security가 authentication 관리하므로 컨트롤러에서 검증할 필요 없음 / 보안 높음
    //jwt 자동으로 생성하여 응답

    //로그인 엔드포인트
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //이렇게 하는 게 맞는지?
        }
        String email = authentication.getName();
        String accessToken = jwtUtil.generateAccessToken(email, "ROLE_USER");

        //로그인 성공시, 사용자 정보가 아니라 토큰만 전달
        //사용자 정보를 함께 줘야 하려나..?
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(accessToken));
    }
}
