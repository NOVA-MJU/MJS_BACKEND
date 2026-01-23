package nova.mjs.domain.mentorship.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.config.webSocket.RedisPublisher;
import nova.mjs.domain.mentorship.chat.event.ChatRoomService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {

    private final RedisPublisher redisPublisher;
    private final ChatRoomRedisRepository chatRoomRedisRepository;
    private final ChatRoomService chatRoomService;

    /**
     * 채팅방에 메시지 발송
     */
    public void sendChatMessage(ChatMessageDto chatMessage, String accessToken) {
        // 0. redis에 해당 채팅방roomId(key)에 마지막 메세지(value)를 넣어준다.
        chatRoomRedisRepository.setLastChatMessage(chatMessage.getRoomId(), chatMessage);

        Long userId = chatMessage.getUserId();
        Long partnerId;

        // 1. 채팅방이 삭제되는 것이라면 delete 를 해준다.
        if (chatMessage.getType().equals(MessageType.DELETE)) {
            chatRoomService.deleteChatRoom(accessToken, chatMessage.getRoomId(), userId);
            chatRoomRedisRepository.deleteChatRoom(userId,chatMessage.getRoomId());
        }

        ChatRoomListGetResponse newChatRoomList = null;
        if (chatRoomRedisRepository.existChatRoom(userId, chatMessage.getRoomId())) {
            newChatRoomList = chatRoomRedisRepository.getChatRoom(userId, chatMessage.getRoomId());
        } else {
            newChatRoomList = chatRoomService.getChatRoomInfo(accessToken, chatMessage.getRoomId());
        }

        partnerId = getPartnerId(chatMessage, newChatRoomList);

        // 2. 채팅방 리스트에 새로운 채팅방 정보가 없다면, 넣어준다. 마지막 메시지도 같이 담는다. 상대방 레디스에도 업데이트 해준다.
        setNewChatRoomInfo(chatMessage, newChatRoomList);

        // 3. 마지막 메시지들이 담긴 채팅방 리스트들을 가져온다.
        List<ChatRoomListGetResponse> chatRoomListGetResponseList = chatRoomService.getChatRoomList(userId, accessToken);
        // 4. 파트너 채팅방 리스트도 가져온다. (파트너는 userId 로만)
        List<ChatRoomListGetResponse> partnerChatRoomGetResponseList = getChatRoomListByUserId(partnerId);

        // 5. 마지막 메세지 기준으로 정렬 채팅방 리스트 정렬
        chatRoomListGetResponseList = chatRoomService.sortChatRoomListLatest(chatRoomListGetResponseList);
        partnerChatRoomGetResponseList = chatRoomService.sortChatRoomListLatest(partnerChatRoomGetResponseList);

        MessageSubDto messageSubDto = MessageSubDto.builder()
                .userId(userId)
                .partnerId(partnerId)
                .chatMessageDto(chatMessage)
                .list(chatRoomListGetResponseList)
                .partnerList(partnerChatRoomGetResponseList)
                .build();

        redisPublisher.publish(messageSubDto);
    }

    private Long getPartnerId(ChatMessageDto chatMessage, ChatRoomListGetResponse roomInfo) {
        // roomInfo에 partnerId가 있다면 그 값을 우선 사용
        if (roomInfo != null && roomInfo.getPartnerId() != null) {
            return roomInfo.getPartnerId();
        }
        // TODO: 실제로는 roomId로 채팅방 참가자 조회해서 "나 아닌 사람"을 파트너로 결정해야 함
        return null;
    }

    private void setNewChatRoomInfo(ChatMessageDto chatMessage, ChatRoomListGetResponse roomInfo) {
        // TODO: roomInfo에 lastMessage/lastMessageTime 갱신 후
        // chatRoomRedisRepository.setChatRoom(userId, roomId, roomInfo) 등을 수행하는 방식으로 구현
        // 현재는 최소 컴파일용 스텁
    }

    private List<ChatRoomListGetResponse> getChatRoomListByUserId(Long userId) {
        // TODO: partner의 채팅방 리스트를 토큰 없이 조회하는 구조인지(서버 내부 조회인지)에 따라 구현 달라짐
        return List.of();
    }

}