package nova.mjs.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    //기본 포트 localhost:6379의 redis 연결 팩토리
    //redis 서버의 연결 설정
    @Bean(name = "keywordRedisConnectionFactory")
    public RedisConnectionFactory redisConnectionFactory(){
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
        connectionFactory.setDatabase(2); //database 2번 사용 - 기본 0(이메일)
        connectionFactory.afterPropertiesSet();
        return connectionFactory; //기본 포트 localhost:6379
    }

    //일반 redis template
    //template은 해당 연결을 통해 데이터 입출력을 수행
    @Bean(name = "keywordRedisTemplate")
    public RedisTemplate<String, String> redisTemplate(
            @Qualifier("keywordRedisConnectionFactory") RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        return template;
    }
}
