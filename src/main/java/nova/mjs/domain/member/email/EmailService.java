package nova.mjs.domain.member.email;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    //private final RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate redisTemplate;

    // 이메일 인증코드 발송
    public String sendVerificationEmail(String email) {
        String code = generateVerificationCode();

        // Redis에 5분 동안 저장
        redisTemplate.opsForValue().set(email, code, Duration.ofMinutes(5));

        // 이메일 전송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[MJS] 이메일 인증코드 안내");
        message.setText("인증 코드: " + code);
        mailSender.send(message);

        return "인증 코드가 이메일로 발송되었습니다.";
    }

    // 이메일 인증코드 검증
    public EmailVerificationResultDto verifyEmailCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(email);
        boolean matched = code.equals(storedCode);
        return new EmailVerificationResultDto(email, matched);
    }

    // 인증코드 생성
    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
