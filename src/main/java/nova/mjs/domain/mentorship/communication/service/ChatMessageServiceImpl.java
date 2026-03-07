package nova.mjs.domain.mentorship.communication.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.dto.ChatMessageDTO;
import nova.mjs.domain.mentorship.communication.entity.ChatRoom;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl {

    private final ChatRoomServiceImpl chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(ChatMessageDTO.Request request) {
        validate(request);

        ChatRoom room = chatRoomService.getByChatUuid(request.getChatUuid());
        chatRoomService.startChatIfWaiting(room);

        ChatMessageDTO.Response response = ChatMessageDTO.Response.builder()
                .chatUuid(room.getChatUuid())
                .senderUuid(request.getSenderUuid())
                .content(request.getContent())
                .sentAt(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + room.getChatUuid(),
                response
        );
    }

    private void validate(ChatMessageDTO.Request request) {
        if (request.getChatUuid() == null) {
            throw new IllegalArgumentException("chatUuid는 필수입니다.");
        }
        if (request.getSenderUuid() == null) {
            throw new IllegalArgumentException("senderUuid는 필수입니다.");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }
    }
}