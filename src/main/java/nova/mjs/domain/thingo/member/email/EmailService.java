package nova.mjs.domain.thingo.member.email;

import lombok.RequiredArgsConstructor;
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
    private final StringRedisTemplate redis;

    private static final String CODE_PREFIX     = "email:verify:";
    private static final String VERIFIED_PREFIX = "recovery:verified:";
    private static final Duration CODE_TTL      = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL  = Duration.ofMinutes(15);

    /** 인증코드 발송 */
    public String sendVerificationEmail(String rawEmail) {
        final String email = normalize(rawEmail);
        final String code  = generateVerificationCode();

        redis.opsForValue().set(codeKey(email), code, CODE_TTL);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("[MJS] 이메일 인증코드 안내");
        msg.setText("인증 코드: " + code);
        mailSender.send(msg);

        return "인증 코드가 이메일로 발송되었습니다.";
    }

    /** 인증코드 검증(성공 시 소각) */
    public EmailVerificationResultDto verifyEmailCode(String rawEmail, String code) {
        final String email  = normalize(rawEmail);
        final String stored = redis.opsForValue().get(codeKey(email));

        final boolean matched = (stored != null && stored.equals(code));
        if (matched) {
            redis.delete(codeKey(email));
        }
        return new EmailVerificationResultDto(email, matched);
    }

    /** 비번찾기용: 인증 성공 플래그 세팅(서버 내부 상태) */
    public void markVerifiedForRecovery(String rawEmail) {
        final String email = normalize(rawEmail);
        redis.opsForValue().set(verifiedKey(email), "1", VERIFIED_TTL);
    }

    /** 비번찾기용: 인증 성공 플래그 확인 */
    public boolean hasVerifiedForRecovery(String rawEmail) {
        final String email = normalize(rawEmail);
        Boolean exists = redis.hasKey(verifiedKey(email));
        return exists != null && exists;
    }

    /** 비번찾기용: 인증 성공 플래그 소각 */
    public void clearVerifiedForRecovery(String rawEmail) {
        final String email = normalize(rawEmail);
        redis.delete(verifiedKey(email));
    }

    // ===== 내부 유틸 =====
    private String codeKey(String email)     { return CODE_PREFIX + email; }
    private String verifiedKey(String email) { return VERIFIED_PREFIX + email; }
    private String normalize(String email)   { return email == null ? null : email.trim().toLowerCase(); }
    private String generateVerificationCode(){ return UUID.randomUUID().toString().substring(0, 6).toUpperCase(); }
}
