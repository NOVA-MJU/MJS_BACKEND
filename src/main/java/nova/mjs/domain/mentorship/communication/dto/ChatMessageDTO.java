package nova.mjs.domain.mentorship.communication.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ChatMessageDTO {

    @Getter
    @NoArgsConstructor
    public static class Request {
        private UUID chatUuid;
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

    @Getter
    @Builder
    public static class HistoryResponse {
        private String messageId;
        private UUID chatUuid;
        private UUID senderUuid;
        private String content;
        private LocalDateTime sentAt;
    }

    @Getter
    @Builder
    public static class HistoryListResponse {
        private UUID chatUuid;
        private int messageCount;
        private List<HistoryResponse> messages;
    }
}