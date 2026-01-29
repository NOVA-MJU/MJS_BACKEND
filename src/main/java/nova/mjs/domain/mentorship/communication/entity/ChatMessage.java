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
}
