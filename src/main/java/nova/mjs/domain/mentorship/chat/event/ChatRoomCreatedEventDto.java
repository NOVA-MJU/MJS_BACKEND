package nova.mjs.domain.mentorship.chat.event;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreatedEventDto {
    private String roomId;
    private UUID menteeUuid; // 요청자(멘티)
    private UUID mentorUuid; // 상대(멘토)
}
