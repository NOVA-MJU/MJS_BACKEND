package nova.mjs.domain.mentorship.chat.event;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.chat.ChatRoomListGetResponse;
import nova.mjs.domain.mentorship.chat.event.ChatRoomDocument;
import nova.mjs.domain.mentorship.chat.event.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    /**
     *  채팅방 생성 또는 기존 방 반환
     *  멘티가 멘토 신청 + 첫 메시지 시작 시점에 호출하면 됨
     */
    public ChatRoomDocument createOrGetRoom(Long userId, Long partnerId) {
        Long userA = Math.min(userId, partnerId);
        Long userB = Math.max(userId, partnerId);

        Optional<ChatRoomDocument> existing = chatRoomRepository.findByUserAAndUserB(userA, userB);
        if (existing.isPresent() && !existing.get().isDeleted()) {
            return existing.get();
        }

        ChatRoomDocument room = ChatRoomDocument.builder()
                .id(java.util.UUID.randomUUID().toString())
                .userA(userA)
                .userB(userB)
                .deleted(false)
                .createdAt(Instant.now())
                .build();

        return chatRoomRepository.save(room);
    }

    /** 채팅방 삭제(소프트 삭제로 구현) */
    public void deleteChatRoom(String accessToken, String roomId, Long userId) {
        ChatRoomDocument room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + roomId));

        // 권한 체크(최소): userId가 방의 참가자여야 함
        if (!isParticipant(room, userId)) {
            throw new IllegalStateException("Not a participant of this room.");
        }

        room.setDeleted(true);
        room.setDeletedAt(Instant.now());
        chatRoomRepository.save(room);
    }

    /** 채팅방 정보 1건 조회(파트너 id 포함) */
    public ChatRoomListGetResponse getChatRoomInfo(String accessToken, String roomId) {
        ChatRoomDocument room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + roomId));

        if (room.isDeleted()) {
            throw new IllegalStateException("Chat room is deleted.");
        }

        // partnerId는 “요청자 기준”으로 계산해야 정확하지만,
        // 현재 시그니처에 userId가 없으니, 이 메서드는 room 자체 정보만 반환하도록 둠.
        // (필요하면 getChatRoomInfo(accessToken, roomId, userId)로 확장 추천)
        return ChatRoomListGetResponse.builder()
                .chatRoomNumber(room.getId())
                .partnerId(null) // 여기서 partnerId 결정하려면 userId가 필요함
                .lastMessage(room.getLastMessage())
                .lastMessageTime(room.getLastMessageTime() == null ? null : room.getLastMessageTime().toString())
                .unreadCount(0)
                .build();
    }

    /** 유저 채팅방 리스트 조회 */
    public List<ChatRoomListGetResponse> getChatRoomList(Long userId, String accessToken) {
        List<ChatRoomDocument> rooms = chatRoomRepository.findByUserAOrUserB(userId, userId);

        return rooms.stream()
                .filter(r -> !r.isDeleted())
                .map(r -> ChatRoomListGetResponse.builder()
                        .chatRoomNumber(r.getId())
                        .partnerId(getPartnerId(r, userId))
                        .lastMessage(r.getLastMessage())
                        .lastMessageTime(r.getLastMessageTime() == null ? null : r.getLastMessageTime().toString())
                        .unreadCount(0) // 읽음처리는 다음 단계에서 설계
                        .build()
                )
                .collect(Collectors.toList());
    }

    /** 최신 메시지 기준 정렬 */
    public List<ChatRoomListGetResponse> sortChatRoomListLatest(List<ChatRoomListGetResponse> list) {
        list.sort(Comparator.comparing(
                ChatRoomListGetResponse::getLastMessageTime,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        return list;
    }

    private boolean isParticipant(ChatRoomDocument room, Long userId) {
        return room.getUserA().equals(userId) || room.getUserB().equals(userId);
    }

    private Long getPartnerId(ChatRoomDocument room, Long userId) {
        if (room.getUserA().equals(userId)) return room.getUserB();
        if (room.getUserB().equals(userId)) return room.getUserA();
        return null;
    }
}
