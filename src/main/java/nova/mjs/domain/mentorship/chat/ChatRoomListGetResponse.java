package nova.mjs.domain.mentorship.chat;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListGetResponse {

    /** 채팅방 식별자(문자열 roomId와 동일하게 쓰거나 별도 번호를 쓰면 그 값) */
    private String chatRoomNumber;

    /** 상대방(파트너) id */
    private Long partnerId;

    /** 마지막 메시지 */
    private String lastMessage;

    /** 마지막 메시지 시간(정렬 기준) - ISO-8601 문자열 권장 */
    private String lastMessageTime;

    /** 안 읽은 메시지 수(선택) */
    private Integer unreadCount;
}
