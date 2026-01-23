package nova.mjs.domain.mentorship.chat.event;

import lombok.*;

/** Redis 단일 토픽은 메시지가 섞이니까, 이게 무슨 이벤트인지 알려주는 용도 **/

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEventEnvelope<T> {
    private ChatEventType type;
    private T data;
}
