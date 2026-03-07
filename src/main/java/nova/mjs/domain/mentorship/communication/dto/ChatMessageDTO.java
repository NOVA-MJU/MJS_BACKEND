package nova.mjs.domain.mentorship.communication.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatMessageDTO {

    @Getter
    @NoArgsConstructor
    public static class Request {

        private UUID chatUuid;
        private UUID senderUuid;
        private String content;
    }

    @Getter
    @Builder
    public static class Response {

        private UUID chatUuid;
        private UUID senderUuid;
        private String content;
        private LocalDateTime sentAt;
    }
}