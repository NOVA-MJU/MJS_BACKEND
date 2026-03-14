package nova.mjs.domain.mentorship.communication.repository;

import nova.mjs.domain.mentorship.communication.entity.ChatRoom;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByChatUuid(UUID chatUuid);

    Optional<ChatRoom> findFirstByRequesterAndResponderAndStatusIn(
            Member requester,
            Member responder,
            List<ChatRoom.ChatStatus> statuses
    );

    List<ChatRoom> findByRequesterOrResponderOrderByCreatedAtDesc(Member requester, Member responder);
}