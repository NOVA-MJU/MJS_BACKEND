package nova.mjs.domain.mentorship.chat.event;

import lombok.RequiredArgsConstructor;
import nova.mjs.config.webSocket.ChatRedisPublisher;
import nova.mjs.domain.mentorship.chat.event.*;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatRoomCreateController {

    private final ChatRoomService chatRoomService;
    private final ChatRedisPublisher chatRedisPublisher;

    @MessageMapping("/chat/room/create")
    public void createRoom(ChatRoomCreateRequestDto req, Principal principal) {

        // 1) 인증 체크: Principal이 없으면 "토큰이 제공되지 않음"으로 보는 게 자연스러움
        if (principal == null || principal.getName() == null) {
            throw new BusinessBaseException(ErrorCode.TOKEN_NOT_PROVIDED);
        }

        Long userId;
        try {
            userId = Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            // Principal에 userId가 아닌 값이 들어온 케이스 = 토큰/인증 흐름이 깨진 것
            throw new BusinessBaseException(ErrorCode.TOKEN_INVALID);
        }

        // 2) 요청 유효성
        if (req == null || req.getPartnerId() == null) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        Long partnerId = req.getPartnerId();
        if (partnerId.equals(userId)) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        // 3) 생성/조회
        ChatRoomDocument room = chatRoomService.createOrGetRoom(userId, partnerId);

        // 4) publish
        ChatRoomCreatedEventDto payload = ChatRoomCreatedEventDto.builder()
                .roomId(room.getId())
                .userId(userId)
                .partnerId(partnerId)
                .build();

        ChatEventEnvelope<ChatRoomCreatedEventDto> envelope = ChatEventEnvelope.<ChatRoomCreatedEventDto>builder()
                .type(ChatEventType.ROOM_CREATED)
                .data(payload)
                .build();

        chatRedisPublisher.publish(envelope);
    }
}
