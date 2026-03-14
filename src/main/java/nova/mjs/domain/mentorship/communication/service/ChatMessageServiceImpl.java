package nova.mjs.domain.mentorship.communication.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.dto.ChatMessageDTO;
import nova.mjs.domain.mentorship.communication.entity.ChatMessage;
import nova.mjs.domain.mentorship.communication.entity.ChatRoom;
import nova.mjs.domain.mentorship.communication.repository.ChatMessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl {

    private final ChatRoomServiceImpl chatRoomService;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(ChatMessageDTO.Request request) {
        validate(request);

        ChatRoom room = chatRoomService.getByChatUuid(request.getChatUuid());
        chatRoomService.startChatIfWaiting(room);

        ChatMessage savedMessage = chatMessageRepository.save(
                ChatMessage.create(
                        room.getChatUuid(),
                        request.getSenderUuid(),
                        request.getContent().trim()
                )
        );

        ChatMessageDTO.Response response = ChatMessageDTO.Response.builder()
                .chatUuid(savedMessage.getChatUuid())
                .senderUuid(savedMessage.getSenderUuid())
                .content(savedMessage.getContent())
                .sentAt(savedMessage.getSentAt())
                .build();

        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + room.getChatUuid(),
                response
        );
    }

    @Transactional(readOnly = true)
    public ChatMessageDTO.HistoryListResponse getMessages(UUID chatUuid) {
        if (chatUuid == null) {
            throw new IllegalArgumentException("chatUuid는 필수입니다.");
        }

        // 채팅방 존재 여부 검증
        chatRoomService.getByChatUuid(chatUuid);

        List<ChatMessageDTO.HistoryResponse> messages = chatMessageRepository
                .findByChatUuidOrderBySentAtAsc(chatUuid)
                .stream()
                .map(message -> ChatMessageDTO.HistoryResponse.builder()
                        .messageId(message.getId())
                        .chatUuid(message.getChatUuid())
                        .senderUuid(message.getSenderUuid())
                        .content(message.getContent())
                        .sentAt(message.getSentAt())
                        .build())
                .toList();

        return ChatMessageDTO.HistoryListResponse.builder()
                .chatUuid(chatUuid)
                .messageCount(messages.size())
                .messages(messages)
                .build();
    }

    private void validate(ChatMessageDTO.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("메시지 요청은 필수입니다.");
        }
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