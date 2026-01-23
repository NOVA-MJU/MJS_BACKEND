package nova.mjs.config.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.mentorship.chat.ChatMessageDto;
import nova.mjs.domain.mentorship.chat.ChatRoomListGetResponse;
import nova.mjs.domain.mentorship.chat.MessageSubDto;
import nova.mjs.domain.mentorship.chat.event.ChatEventEnvelope;
import nova.mjs.domain.mentorship.chat.event.ChatEventType;
import nova.mjs.domain.mentorship.chat.event.ChatRoomCreatedEventDto;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    // RedisConfig에서 MessageListenerAdapter(subscriber, "sendMessage")로 연결되어 있음
    public void sendMessage(String publishMessage) {
        try {
            ChatEventEnvelope<?> envelope =
                    objectMapper.readValue(publishMessage, ChatEventEnvelope.class);

            if (envelope.getType() == ChatEventType.ROOM_CREATED) {
                // data를 다시 타입 캐스팅해서 읽어야 함(제네릭 때문에)
                ChatRoomCreatedEventDto payload = objectMapper.convertValue(
                        envelope.getData(),
                        ChatRoomCreatedEventDto.class
                );

                // 양쪽 유저에게 roomId 알려주기
                messagingTemplate.convertAndSend(
                        "/sub/chat/room-created/" + payload.getUserId(), payload
                );
                messagingTemplate.convertAndSend(
                        "/sub/chat/room-created/" + payload.getPartnerId(), payload
                );
                return;
            }

            // 다른 이벤트는 지금 단계에서는 무시 (나중에 CHAT_MESSAGE 등 추가)
        } catch (Exception e) {
            log.error("RedisSubscriber error", e);
        }
    }


    public void sendRoomList(String publishMessage) {
        try {
            MessageSubDto dto = objectMapper.readValue(publishMessage, MessageSubDto.class);

            List<ChatRoomListGetResponse> chatRoomListGetResponseList = dto.getList();
            List<ChatRoomListGetResponse> chatRoomListGetResponseListPartner = dto.getPartnerList();

            Long userId = dto.getUserId();
            Long partnerId = dto.getPartnerId();

            // 로그인 유저 채팅방 리스트 최신화 -> 내 계정에 보냄
            messagingTemplate.convertAndSend(
                    "/sub/chat/roomlist/" + userId, chatRoomListGetResponseList
            );

            // 파트너 계정에도 리스트 최신화 보냄.
            messagingTemplate.convertAndSend(
                    "/sub/chat/roomlist/" + partnerId, chatRoomListGetResponseListPartner
            );

        } catch (Exception e) {
            log.error("Exception {}", e);
        }
    }
}