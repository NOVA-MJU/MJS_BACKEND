package nova.mjs.domain.mentorship.chat.event;

import nova.mjs.domain.mentorship.chat.event.ChatRoomDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends MongoRepository<ChatRoomDocument, String> {

    Optional<ChatRoomDocument> findByUserAAndUserB(Long userA, Long userB);

    List<ChatRoomDocument> findByUserAOrUserB(Long userA, Long userB);
}
