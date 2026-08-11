package nova.mjs.domain.thingo.member.controller;

import nova.mjs.domain.thingo.member.email.EmailService;
import nova.mjs.domain.thingo.member.email.EmailVerificationRequestDto;
import nova.mjs.domain.thingo.member.service.command.MemberCommandService;
import nova.mjs.domain.thingo.member.service.query.MemberQueryService;
import nova.mjs.util.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberControllerRecoveryEmailTest {

    private MemberQueryService memberQueryService;
    private EmailService emailService;
    private MemberController controller;

    @BeforeEach
    void setUp() {
        memberQueryService = mock(MemberQueryService.class);
        emailService = mock(EmailService.class);
        controller = new MemberController(
                memberQueryService,
                mock(MemberCommandService.class),
                emailService
        );
    }

    @Test
    void getRecoveryEmailOnlyChecksRegisteredMember() {
        String email = "kimgusqls1@mju.ac.kr";

        ResponseEntity<ApiResponse<String>> response = controller.sendRecoveryEmail(email);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo("가입된 이메일입니다.");
        verify(memberQueryService).validateEmailDomain(email);
        verify(memberQueryService).getMemberByEmail(email);
        verify(emailService, never()).sendVerificationEmail(email);
    }

    @Test
    void postRecoveryEmailSendsVerificationEmail() throws Exception {
        String email = "kimgusqls1@mju.ac.kr";
        EmailVerificationRequestDto request = new EmailVerificationRequestDto();
        Field emailField = EmailVerificationRequestDto.class.getDeclaredField("email");
        emailField.setAccessible(true);
        emailField.set(request, email);
        when(emailService.sendVerificationEmail(email)).thenReturn("인증 코드가 이메일로 발송되었습니다.");

        ResponseEntity<ApiResponse<String>> response = controller.sendRecoveryEmail(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo("인증 코드가 이메일로 발송되었습니다.");
        verify(memberQueryService).validateEmailDomain(email);
        verify(emailService).sendVerificationEmail(email);
    }
}
