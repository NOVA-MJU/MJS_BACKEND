package nova.mjs.util.email;

import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<String> sendEmail(@RequestParam String email) {
        String code = generateVerificationCode();
        emailService.sendVerificationEmail(email, code);

        // Redis에 5분 저장 (TTL)
        redisTemplate.opsForValue().set(email, code, 5, java.util.concurrent.TimeUnit.MINUTES);

        return ResponseEntity.ok("인증 코드가 이메일로 발송되었습니다.");
    }

    @PostMapping("/check")
    public ResponseEntity<Boolean> checkEmailCode(
            @RequestParam String email,
            @RequestBody EmailCheckRequestDto request) {

        String code = request.getCode();
        String redisCode = redisTemplate.opsForValue().get(email);

        System.out.println("요청받은 이메일: " + email);
        System.out.println("요청받은 코드: " + code);
        System.out.println("Redis에 저장된 코드: " + redisCode);

        boolean result = code.equals(redisCode);
        return ResponseEntity.ok(result);
    }


    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
