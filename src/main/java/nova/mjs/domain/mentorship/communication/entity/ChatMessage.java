package nova.mjs.domain.mentorship.communication.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

// MongoDB
@Document(collection = "chat_messages")
@Getter
public class ChatMessage {

    @Id
    private String id;

    private UUID chatUuid;
    private UUID senderUuid;
    private String content;
    private LocalDateTime sentAt;

    private ChatMessage(UUID chatUuid, UUID senderUuid, String content, LocalDateTime sentAt) {
        this.chatUuid = chatUuid;
        this.senderUuid = senderUuid;
        this.content = content;
        this.sentAt = sentAt;
    }

    public static ChatMessage create(UUID chatUuid, UUID senderUuid, String content) {
        return new ChatMessage(chatUuid, senderUuid, content, LocalDateTime.now());
    }
}
