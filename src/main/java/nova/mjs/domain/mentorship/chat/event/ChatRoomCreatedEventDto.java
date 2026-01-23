package nova.mjs.domain.mentorship.chat.event;

import lombok.*;


/** 룸 만들어지면 양쪽 모두에게 알려줄 payload **/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreatedEventDto {
    private String roomId;
    private Long userId;     // 요청자(멘티)
    private Long partnerId;  // 상대(멘토)
}
