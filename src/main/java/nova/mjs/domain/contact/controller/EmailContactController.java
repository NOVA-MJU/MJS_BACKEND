package nova.mjs.domain.contact.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.contact.EmailContactService;
import nova.mjs.domain.contact.dto.EmailContactDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact/email")
@RequiredArgsConstructor
public class EmailContactController {

    private final EmailContactService emailContactService;

    /**
     * 이메일 문의 전송 API
     * @param request 사용자 입력 정보 (fromEmail, subject, content)
     * @return 성공/실패 여부 메시지
     */
    @PostMapping
    public ResponseEntity<EmailContactDTO.Response> sendContactEmail(
            @Valid @RequestBody EmailContactDTO.Request request
    ) {
        EmailContactDTO.Response response = emailContactService.sendContactEmail(request);
        return ResponseEntity.ok(response);
    }
}

