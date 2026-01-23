package nova.mjs.config;

import nova.mjs.config.webSocket.RedisSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private final RedisProperties redisProperties;


    //기본 포트 localhost:6379의 redis 연결 팩토리
    //redis 서버의 연결 설정
    @Primary   // StringRedisTemplate 만들 때 매개변수로 RedisConnectionFactory를 받으면, 어느 팩토리를 넣어야 하는지 스프링/IDE가 결정 못 해서 오류가 . 따라서 하나에 primary 추가
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

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    // Lombok(@RequiredArgsConstructor) 없이 생성자 주입
    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * (추가) 채팅 pub/sub 토픽
     * - 단일 토픽을 쓸 경우
     */
    @Bean(name = "chatChannelTopic")
    public ChannelTopic chatChannelTopic() {
        return new ChannelTopic("chatroom");
    }

    /**
     * (추가) 채팅용 RedisConnectionFactory
     * - 기존 keywordRedisConnectionFactory(DB=2)와 분리
     * - yml의 spring.data.redis.host/port를 사용
     */
    @Bean(name = "chatRedisConnectionFactory")
    public RedisConnectionFactory chatRedisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redisProperties.getHost(),
                redisProperties.getPort()
        );
        // 필요하면 DB 분리 가능 (예: factory.setDatabase(3);)
        factory.afterPropertiesSet();
        return factory;
    }

    /**
     * (추가) 실제 subscriber의 메서드(sendMessage)를 호출하도록 연결
     * - RedisSubscriber 클래스에 sendMessage(String message) 같은 메서드가 있어야 함
     */
    @Bean(name = "listenerAdapterChatMessage")
    public MessageListenerAdapter listenerAdapterChatMessage(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }

    /**
     * (추가) Redis pub/sub 리스너 컨테이너
     * - chatRedisConnectionFactory로 구독 연결
     * - chatChannelTopic 토픽을 구독
     */
    @Bean(name = "redisMessageListenerContainer")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatRedisConnectionFactory") RedisConnectionFactory chatFactory,
            @Qualifier("listenerAdapterChatMessage") MessageListenerAdapter listenerAdapter,
            @Qualifier("chatChannelTopic") ChannelTopic topic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(chatFactory);
        container.addMessageListener(listenerAdapter, topic);
        return container;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerRoomList (
            MessageListenerAdapter listenerAdapterChatRoomList,
            ChannelTopic channelTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(listenerAdapterChatRoomList, channelTopic);
        return container;
    }

    /** 실제 메시지 방을 처리하는 subscriber 설정 추가 */
    @Bean
    public MessageListenerAdapter listenerAdapterChatRoomList(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendRoomList");
    }


    /** Redis에 채팅방 리스트 객체를 저장하기 위해서 메서드 추가 **/
    @Bean(name = "chatObjectRedisTemplate")
    public RedisTemplate<String, Object> chatObjectRedisTemplate(
            @Qualifier("chatRedisConnectionFactory") RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }
}
