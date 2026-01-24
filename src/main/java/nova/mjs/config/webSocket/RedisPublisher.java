package nova.mjs.config.webSocket;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.chat.MessageSubDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisPublisher {
    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;

    public void publish(MessageSubDto message) {
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }
}