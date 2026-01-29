package nova.mjs.domain.mentorship.chat.event;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreateRequestDto {
    private UUID mentorUuid; // 상대방(멘토) UUID
}