package nova.mjs.member.email;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    public void sendVerificationEmail(String toEmail, String code) {
        // 1. Redis에 이메일 주소를 키로, 인증코드를 값으로 저장 (기존 값 있으면 덮어씀)
        redisTemplate.opsForValue().set(toEmail, code, Duration.ofMinutes(5));  // ⏰ 5분 유효

        // 2. 메일 전송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[MJS] 이메일 인증코드 안내");
        message.setText("인증 코드: " + code);
        mailSender.send(message);
    }
}