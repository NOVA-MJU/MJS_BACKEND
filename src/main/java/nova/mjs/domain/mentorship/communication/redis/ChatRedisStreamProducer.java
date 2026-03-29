package nova.mjs.domain.mentorship.communication.redis;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.event.ChatMessageEvent;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatRedisStreamProducer {

    private final StringRedisTemplate stringRedisTemplate;

    public RecordId enqueue(ChatMessageEvent event) {

        //enqueque 실패 시 broadcasting 하면 안 되므로

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("messageId", event.getMessageId());
        payload.put("chatUuid", event.getChatUuid().toString());
        payload.put("senderUuid", event.getSenderUuid().toString());
        payload.put("content", event.getContent());
        payload.put("sentAt", event.getSentAt().toString());

        RecordId recordId = stringRedisTemplate.opsForStream()
                .add(StreamRecords.mapBacked(payload)
                        .withStreamKey(ChatRedisStreamConstants.STREAM_KEY));

        if (recordId == null) {
            throw new IllegalStateException("Redis Stream enqueue에 실패했습니다.");
        }

        return recordId;
    }
}