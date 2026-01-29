package nova.mjs.domain.mentorship.chat.event;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.chat.ChatRoomListGetResponse;
import nova.mjs.util.exception.BusinessBaseException;
import nova.mjs.util.exception.ErrorCode;
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
     * 채팅방 생성 또는 기존 방 반환
     * - userA/userB 정규화로 "두 사람 조합은 한 방" 보장
     */
    public ChatRoomDocument createOrGetRoom(Long userId, Long partnerId) {
        if (userId == null || partnerId == null) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

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

        try {
            return chatRoomRepository.save(room);
        } catch (Exception e) {
            throw new BusinessBaseException(ErrorCode.DATABASE_ERROR);
        }
    }

    /**
     * 채팅방 삭제(소프트 삭제)
     * - 네 ErrorCode에 "ROOM_NOT_FOUND", "FORBIDDEN" 같은 게 없으므로
     *   여기서는 INVALID_REQUEST로 처리
     */
    public void deleteChatRoom(String roomId, Long userId) {
        if (roomId == null || roomId.isBlank() || userId == null) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        ChatRoomDocument room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessBaseException(ErrorCode.INVALID_REQUEST));

        // 이미 삭제된 방이면 멱등 처리(원하면 INVALID_REQUEST로 바꿔도 됨)
        if (room.isDeleted()) {
            return;
        }

        // 참가자가 아니면 삭제 불가
        if (!isParticipant(room, userId)) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        room.setDeleted(true);
        room.setDeletedAt(Instant.now());

        try {
            chatRoomRepository.save(room);
        } catch (Exception e) {
            throw new BusinessBaseException(ErrorCode.DATABASE_ERROR);
        }
    }

    /**
     * 채팅방 정보 1건 조회 (partnerId 정확 계산 가능)
     */
    public ChatRoomListGetResponse getChatRoomInfo(String roomId, Long userId) {
        if (roomId == null || roomId.isBlank() || userId == null) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        ChatRoomDocument room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessBaseException(ErrorCode.INVALID_REQUEST));

        if (room.isDeleted()) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        if (!isParticipant(room, userId)) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        return ChatRoomListGetResponse.builder()
                .chatRoomNumber(room.getId())
                .partnerId(getPartnerId(room, userId))
                .lastMessage(room.getLastMessage())
                .lastMessageTime(room.getLastMessageTime() == null ? null : room.getLastMessageTime().toString())
                .unreadCount(0)
                .build();
    }

    /**
     * 유저 채팅방 리스트 조회
     */
    public List<ChatRoomListGetResponse> getChatRoomList(Long userId) {
        if (userId == null) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        List<ChatRoomDocument> rooms;
        try {
            rooms = chatRoomRepository.findByUserAOrUserB(userId, userId);
        } catch (Exception e) {
            throw new BusinessBaseException(ErrorCode.DATABASE_ERROR);
        }

        return rooms.stream()
                .filter(r -> !r.isDeleted())
                .map(r -> ChatRoomListGetResponse.builder()
                        .chatRoomNumber(r.getId())
                        .partnerId(getPartnerId(r, userId))
                        .lastMessage(r.getLastMessage())
                        .lastMessageTime(r.getLastMessageTime() == null ? null : r.getLastMessageTime().toString())
                        .unreadCount(0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 최신 메시지 기준 정렬
     */
    public List<ChatRoomListGetResponse> sortChatRoomListLatest(List<ChatRoomListGetResponse> list) {
        if (list == null) {
            throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
        }

        list.sort(Comparator.comparing(
                ChatRoomListGetResponse::getLastMessageTime,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        return list;
    }

    private boolean isParticipant(ChatRoomDocument room, Long userId) {
        return userId != null && (room.getUserA().equals(userId) || room.getUserB().equals(userId));
    }

    private Long getPartnerId(ChatRoomDocument room, Long userId) {
        if (room.getUserA().equals(userId)) return room.getUserB();
        if (room.getUserB().equals(userId)) return room.getUserA();
        return null;
    }

    // -----------------------------
    // 기존 시그니처 유지용 호환 메서드 (원하면 지워도 됨)
    // -----------------------------

    /** @deprecated accessToken 안 쓰므로 정식 시그니처(roomId, userId)로 호출부 정리 추천 */
    @Deprecated
    public void deleteChatRoom(String accessToken, String roomId, Long userId) {
        deleteChatRoom(roomId, userId);
    }

    /** @deprecated userId 없으면 partnerId 못 구함. getChatRoomInfo(roomId, userId)로 바꾸는 게 정답 */
    @Deprecated
    public ChatRoomListGetResponse getChatRoomInfo(String accessToken, String roomId) {
        throw new BusinessBaseException(ErrorCode.INVALID_REQUEST);
    }

    /** @deprecated accessToken 안 쓰므로 정식 시그니처(userId)로 호출부 정리 추천 */
    @Deprecated
    public List<ChatRoomListGetResponse> getChatRoomList(Long userId, String accessToken) {
        return getChatRoomList(userId);
    }
}
