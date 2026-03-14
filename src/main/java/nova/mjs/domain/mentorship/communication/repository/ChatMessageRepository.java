package nova.mjs.domain.mentorship.communication.repository;

import nova.mjs.domain.mentorship.communication.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    void deleteByChatUuid(UUID chatUuid);

    List<ChatMessage> findByChatUuidOrderBySentAtAsc(UUID chatUuid);
}