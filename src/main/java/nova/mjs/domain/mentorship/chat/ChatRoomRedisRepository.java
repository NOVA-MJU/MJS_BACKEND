package nova.mjs.domain.mentorship.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomRedisRepository {

    private static final String CHAT_ROOM_KEY = "_CHAT_ROOM_RESPONSE_LIST"; // userId -> (roomId -> ChatRoomListGetResponse)
    private static final String LAST_MESSAGE_KEY = "_LAST_CHAT_MESSAGE";    // (roomId -> ChatMessageDto)

    @Qualifier("chatObjectRedisTemplate")
    private final RedisTemplate<String, Object> chatObjectRedisTemplate;

    private final ObjectMapper objectMapper;

    private HashOperations<String, String, Object> opsHash;

    @PostConstruct
    public void init() {
        this.opsHash = chatObjectRedisTemplate.opsForHash();
    }

    private String getChatRoomKey(Long userId) {
        return userId + CHAT_ROOM_KEY;
    }

    private String getLastMessageKey() {
        return LAST_MESSAGE_KEY;
    }

    /** 채팅방 정보 1건 저장(유저별 채팅방 리스트 hash) */
    public void setChatRoom(Long userId, String chatRoomNumber, ChatRoomListGetResponse value) {
        opsHash.put(getChatRoomKey(userId), chatRoomNumber, value);
    }

    /** 채팅방 1건 조회 */
    public ChatRoomListGetResponse getChatRoom(Long userId, String chatRoomNumber) {
        Object raw = opsHash.get(getChatRoomKey(userId), chatRoomNumber);
        return raw == null ? null : objectMapper.convertValue(raw, ChatRoomListGetResponse.class);
    }

    /** 채팅방 존재 여부 */
    public boolean existChatRoom(Long userId, String chatRoomNumber) {
        return Boolean.TRUE.equals(opsHash.hasKey(getChatRoomKey(userId), chatRoomNumber));
    }

    /** 채팅방 삭제 */
    public void deleteChatRoom(Long userId, String chatRoomNumber) {
        opsHash.delete(getChatRoomKey(userId), chatRoomNumber);
    }

    /** 채팅방 리스트 초기화 */
    public void initChatRoomList(Long userId, List<ChatRoomListGetResponse> list) {
        String key = getChatRoomKey(userId);
        if (Boolean.TRUE.equals(chatObjectRedisTemplate.hasKey(key))) {
            chatObjectRedisTemplate.delete(key);
        }
        for (ChatRoomListGetResponse res : list) {
            setChatRoom(userId, res.getChatRoomNumber(), res);
        }
    }

    /** 채팅방 리스트 조회 */
    public List<ChatRoomListGetResponse> getChatRoomList(Long userId) {
        List<Object> values = opsHash.values(getChatRoomKey(userId));
        return objectMapper.convertValue(values, new TypeReference<List<ChatRoomListGetResponse>>() {});
    }

    /** roomId 기준 마지막 메시지 저장 */
    public void setLastChatMessage(String roomId, ChatMessageDto chatMessageDto) {
        opsHash.put(getLastMessageKey(), roomId, chatMessageDto);
    }

    /** roomId 기준 마지막 메시지 조회(선택) */
    public ChatMessageDto getLastChatMessage(String roomId) {
        Object raw = opsHash.get(getLastMessageKey(), roomId);
        return raw == null ? null : objectMapper.convertValue(raw, ChatMessageDto.class);
    }
}
