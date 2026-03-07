package nova.mjs.domain.mentorship.communication.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.entity.ChatRoom;
import nova.mjs.domain.mentorship.communication.repository.ChatRoomRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceImpl {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public UUID requestChat(Member requester, Member mentor) {
        ChatRoom room = ChatRoom.create(requester, mentor);
        chatRoomRepository.save(room);
        return room.getChatUuid();
    }

    public ChatRoom getByChatUuid(UUID chatUuid) {
        return chatRoomRepository.findByChatUuid(chatUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다. chatUuid=" + chatUuid));
    }

    @Transactional
    public void startChatIfWaiting(ChatRoom room) {
        if (room.getStatus() == ChatRoom.ChatStatus.WAITING) {
            room.startChat();
        }
    }
}