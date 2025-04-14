package nova.mjs.member.email;

import lombok.RequiredArgsConstructor;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/member/email")
public class EmailController {

    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate; // ✅ 이거 추가해줘야 함

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> sendEmail(@RequestParam String email) {
        String code = generateVerificationCode();
        emailService.sendVerificationEmail(email, code);
        redisTemplate.opsForValue().set(email, code, 5, java.util.concurrent.TimeUnit.MINUTES);

        return ResponseEntity.ok(ApiResponse.success("인증 코드가 이메일로 발송되었습니다."));
    }


    @PostMapping("/check")
    public ResponseEntity<ApiResponse<EmailVerificationResultDto>> checkEmailCode(
            @RequestParam String email,
            @RequestBody EmailCheckRequestDto request) {

        String code = request.getCode();
        String redisCode = redisTemplate.opsForValue().get(email);

        boolean result = code.equals(redisCode);
        EmailVerificationResultDto response = new EmailVerificationResultDto(email, result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }




    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
