package nova.mjs.domain.mentorship.communication.repository;

import nova.mjs.domain.mentorship.communication.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByChatUuid(UUID chatUuid);
}