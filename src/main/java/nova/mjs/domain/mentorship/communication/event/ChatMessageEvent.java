package nova.mjs.domain.mentorship.communication.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEvent {
    //redis stream에 넣는 payload와 mongoDB 저장 payload를 분리하기 위한 객체

    private String messageId;
    private UUID chatUuid;
    private UUID senderUuid;
    private String content;
    private LocalDateTime sentAt;
}