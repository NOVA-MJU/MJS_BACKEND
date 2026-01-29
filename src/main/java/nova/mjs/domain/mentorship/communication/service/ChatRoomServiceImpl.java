package nova.mjs.domain.mentorship.communication.service;

import nova.mjs.domain.mentorship.communication.entity.ChatRoom;
import nova.mjs.domain.mentorship.communication.repository.ChatRoomRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ChatRoomServiceImpl {
    static private ChatRoomRepository chatRoomRepository;

    @Transactional
    public UUID requestChat(Member requester, Member mentor) {
        ChatRoom room = ChatRoom.create(requester, mentor);
        chatRoomRepository.save(room);
        return room.getChatUuid();
    }

}
