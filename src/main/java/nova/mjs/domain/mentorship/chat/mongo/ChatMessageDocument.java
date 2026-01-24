package nova.mjs.domain.mentorship.chat.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_message")
public class ChatMessageDocument {

    @Id
    private String id;

    private String roomId;
    private Long userId;
    private String message;
    private String time;       // ISO-8601 권장
    private String type;       // enum을 string으로 저장 (MessageType.name())
    private long userCount;
    private int negoPrice;
}
