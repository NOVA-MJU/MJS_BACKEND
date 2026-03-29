package nova.mjs.domain.mentorship.communication.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.entity.ChatMessage;
import nova.mjs.domain.mentorship.communication.event.ChatMessageEvent;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessagePersistenceService {

    //consumer가 받은 이벤트들을 MongoTemplate bulk upsert로 반영

    private final MongoTemplate mongoTemplate;

    public void persistBatch(List<ChatMessageEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ChatMessage.class);

        for (ChatMessageEvent event : events) {
            Query query = Query.query(Criteria.where("_id").is(event.getMessageId()));

            Update update = new Update()
                    .setOnInsert("_id", event.getMessageId())
                    .setOnInsert("chatUuid", event.getChatUuid())
                    .setOnInsert("senderUuid", event.getSenderUuid())
                    .setOnInsert("content", event.getContent())
                    .setOnInsert("sentAt", event.getSentAt());

            //upsert + setOnInsert는 이미 저장된 메시지는 건드리지 않고, 없는 메시지만 넣음
            //upsert : update + insert : 조건에 맞는 문서가 이미 있으면 update, 없으면 insert
            //setOnInsert : 문서가 이미 있으면 바꾸지 않음 - redis consumer 과정에서 ACK가 안 되면 저장이 안 되므로 같은 메시지 다시 처리 -> 이미 있으면 아무것도 안 함
            bulkOps.upsert(query, update);
        }

        bulkOps.execute();
    }
}