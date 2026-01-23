package nova.mjs.config.webSocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.chat.event.ChatEventEnvelope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRedisPublisher {

    private final ChannelTopic chatChannelTopic;       // RedisConfig의 @Bean(name="chatChannelTopic")
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(ChatEventEnvelope<?> envelope) {
        try {
            String json = objectMapper.writeValueAsString(envelope);
            stringRedisTemplate.convertAndSend(chatChannelTopic.getTopic(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize chat envelope", e);
        }
    }
}
