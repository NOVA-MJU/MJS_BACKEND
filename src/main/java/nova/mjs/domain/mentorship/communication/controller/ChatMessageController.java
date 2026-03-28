package nova.mjs.domain.mentorship.communication.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.dto.ChatMessageDTO;
import nova.mjs.domain.mentorship.communication.service.ChatMessageServiceImpl;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageServiceImpl chatMessageService;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDTO.Request request, Principal principal) {

        //요청 DTO에서 sender의 아이디를 꺼내는 게 아니라, principal에서 사용자 UUID를 꺼냄
                Authentication authentication = (Authentication) principal;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID authenticatedUserUuid = userPrincipal.getUuid();

        chatMessageService.sendMessage(request, authenticatedUserUuid);
    }
}