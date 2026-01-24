package nova.mjs.domain.mentorship.chat.event;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_rooms")
@CompoundIndex(name = "uniq_pair", def = "{'userA': 1, 'userB': 1}", unique = true)
public class ChatRoomDocument {

    @Id
    private String id;          // roomId로 사용 (Mongo ObjectId가 아니라 String UUID도 가능)

    private Long userA;         // 작은 ID를 넣는 방식 추천 (정렬된 pair)
    private Long userB;

    // 리스트용 메타
    private String lastMessage;
    private Instant lastMessageTime;

    // 소프트 삭제용(선택)
    private boolean deleted;
    private Instant deletedAt;

    private Instant createdAt;
}
