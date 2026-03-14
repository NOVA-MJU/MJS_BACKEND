package nova.mjs.domain.mentorship.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.domain.mentorship.communication.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatRoomDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private UUID requesterUuid;
        private UUID responderUuid;
    }

    @Getter
    @Builder
    public static class CreateResponse {
        private UUID chatUuid;
        private UUID requesterUuid;
        private UUID responderUuid;
        private ChatRoom.ChatStatus status;
    }

    @Getter
    @Builder
    public static class DeleteResponse {
        private UUID chatUuid;
        private String message;
    }

    @Getter
    @Builder
    public static class SummaryResponse {
        private UUID chatUuid;
        private UUID myUuid;
        private UUID partnerUuid;
        private String partnerName;
        private String partnerProfileImageUrl;
        private ChatRoom.ChatStatus status;
        private LocalDateTime createdAt;
    }
}