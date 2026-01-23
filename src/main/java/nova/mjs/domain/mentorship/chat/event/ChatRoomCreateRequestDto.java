package nova.mjs.domain.mentorship.chat.event;

import lombok.*;

/** 룸 생성 요청에 필요한 최소 정보(상대 userId).
    (멘티가 멘토에게 신청했다면 mentorId를 넣는 식) **/

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreateRequestDto {
    private Long partnerId; // 상대방 userId (멘토)
}
