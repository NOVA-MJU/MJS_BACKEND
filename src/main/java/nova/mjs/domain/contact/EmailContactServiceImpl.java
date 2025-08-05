package nova.mjs.domain.contact;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.contact.dto.EmailContactDTO;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailContactServiceImpl implements EmailContactService {

    private final JavaMailSender mailSender;
    private static final String ADMIN_EMAIL = "mjsearch2025@gmail.com";

    @Override
    public EmailContactDTO.Response sendContactEmail(EmailContactDTO.Request request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // ADMIN 계정으로 발신
            helper.setTo(ADMIN_EMAIL);
            helper.setFrom(ADMIN_EMAIL);
            helper.setSubject("[MJS 문의] " + request.getSubject());
            helper.setText(request.getContent(), true);  // HTML 허용

            mailSender.send(message);

            log.info("이메일 문의 전송 완료: subject = {}", request.getSubject());
            return EmailContactDTO.Response.builder()
                    .success(true)
                    .message("문의가 성공적으로 전송되었습니다.")
                    .build();

        } catch (MessagingException e) {
            log.error("이메일 문의 전송 실패: {}", e.getMessage(), e);
            return EmailContactDTO.Response.builder()
                    .success(false)
                    .message("이메일 전송 중 오류가 발생했습니다.")
                    .build();
        }
    }
}
