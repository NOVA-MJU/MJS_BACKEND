package nova.mjs.domain.mentorship.chat.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageMongoRepository
        extends MongoRepository<ChatMessageDocument, String> {
}
