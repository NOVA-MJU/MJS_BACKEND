package nova.mjs.domain.mentorship.communication.redis;

public final class ChatRedisStreamConstants {

    private ChatRedisStreamConstants() {
    }

    //키 이름 관리 - 다중 consumer 도입 시 편리

    public static final String STREAM_KEY = "stream:chat:persist";
    public static final String GROUP_NAME = "chat-persist-group";
    public static final String CONSUMER_NAME = "chat-persist-consumer-1";
    public static final int BATCH_SIZE = 100;
}