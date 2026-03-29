package nova.mjs.domain.mentorship.communication.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Document(collection = "chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    private String id; // 메시지 이벤트 ID = Mongo _id -> 비동기로 처리하면 동일 메시지 들어올 수 있으므로 중복 제어

    private UUID chatUuid;
    private UUID senderUuid;
    private String content;
    private LocalDateTime sentAt;

    @Builder
    private ChatMessage(String id, UUID chatUuid, UUID senderUuid, String content, LocalDateTime sentAt) {
        this.id = id;
        this.chatUuid = chatUuid;
        this.senderUuid = senderUuid;
        this.content = content;
        this.sentAt = sentAt;
    }

    public static ChatMessage create(
            String messageId,
            UUID chatUuid,
            UUID senderUuid,
            String content,
            LocalDateTime sentAt
    ) {
        return ChatMessage.builder()
                .id(messageId)
                .chatUuid(chatUuid)
                .senderUuid(senderUuid)
                .content(content)
                .sentAt(sentAt)
                .build();
    }
}