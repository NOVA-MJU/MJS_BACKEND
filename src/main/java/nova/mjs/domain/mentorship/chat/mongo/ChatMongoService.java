package nova.mjs.domain.mentorship.chat.mongo;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.chat.ChatMessageDto;
import nova.mjs.domain.mentorship.chat.mongo.ChatMessageDocument;
import nova.mjs.domain.mentorship.chat.mongo.ChatMessageMongoRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ChatMongoService {

    private final ChatMessageMongoRepository chatMessageMongoRepository;

    public ChatMessageDto save(ChatMessageDto message) {
        // time이 비어있으면 서버에서 채워줌(정렬/표시 안정성)
        if (message.getTime() == null || message.getTime().isBlank()) {
            message.setTime(OffsetDateTime.now().toString());
        }

        ChatMessageDocument doc = ChatMessageDocument.builder()
                .roomId(message.getRoomId())
                .userId(message.getUserId())
                .message(message.getMessage())
                .time(message.getTime())
                .type(message.getType() == null ? null : message.getType().name())
                .userCount(message.getUserCount())
                .negoPrice(message.getNegoPrice())
                .build();

        chatMessageMongoRepository.save(doc);
        return message;
    }
}
