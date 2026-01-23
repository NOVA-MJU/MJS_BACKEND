package nova.mjs.domain.mentorship.chat;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.chat.mongo.ChatMongoService;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatMongoService chatMongoService;
    private final ChatService chatService;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message,
                        @Header("Authorization") String accessToken
    ) {
        ChatMessageDto chatMessageDto = chatMongoService.save(message);
        chatService.sendChatMessage(chatMessageDto, accessToken); //RedisPublisher 호출
    }
}