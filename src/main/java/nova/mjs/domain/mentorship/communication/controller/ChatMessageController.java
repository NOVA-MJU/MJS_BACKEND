package nova.mjs.domain.mentorship.communication.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.dto.ChatMessageDTO;
import nova.mjs.domain.mentorship.communication.service.ChatMessageServiceImpl;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageServiceImpl chatMessageService;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDTO.Request request) {
        chatMessageService.sendMessage(request);
    }

}