package nova.mjs.domain.mentorship.communication.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.mentorship.communication.redis.ChatRedisStreamConstants;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RedisStreamConfig {

    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public ApplicationRunner chatStreamInitializer() {
        return args -> {
            Boolean exists = stringRedisTemplate.hasKey(ChatRedisStreamConstants.STREAM_KEY);

            if (Boolean.FALSE.equals(exists)) {
                stringRedisTemplate.opsForStream().add(
                        StreamRecords.mapBacked(Map.of("_init", "0"))
                                .withStreamKey(ChatRedisStreamConstants.STREAM_KEY)
                );
            }

            try {
                stringRedisTemplate.opsForStream().createGroup(
                        ChatRedisStreamConstants.STREAM_KEY,
                        ReadOffset.from("0"),
                        ChatRedisStreamConstants.GROUP_NAME
                );
                log.info("Redis Stream consumer group 생성 완료. group={}", ChatRedisStreamConstants.GROUP_NAME);
            } catch (Exception e) {
                log.info("Redis Stream consumer group이 이미 존재하거나 생성 불필요합니다. group={}",
                        ChatRedisStreamConstants.GROUP_NAME);
            }
        };
    }
}