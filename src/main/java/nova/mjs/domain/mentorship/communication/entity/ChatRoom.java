package nova.mjs.domain.mentorship.communication.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.util.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID chatUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member requester;   // 대화 신청자

    @ManyToOne(fetch = FetchType.LAZY)
    private Member responder;   // 멘토

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatStatus status;

    public enum ChatStatus {
        WAITING,       // 대기
        IN_PROGRESS,   // 진행 중
        COMPLETED      // 완료
    }


    protected ChatRoom(Member requester, Member responder) {
        this.chatUuid = UUID.randomUUID();
        this.requester = requester;
        this.responder = responder;
        this.status = ChatStatus.WAITING;
    }

    public static ChatRoom create(Member requester, Member responder) {
        return new ChatRoom(requester, responder);
    }

    /* 상태 전이 */
    public void startChat() {
        if (this.status != ChatStatus.WAITING) {
            throw new IllegalStateException("대화를 시작할 수 없는 상태입니다.");
        }
        this.status = ChatStatus.IN_PROGRESS;
    }

    public void completeChat() {
        if (this.status != ChatStatus.IN_PROGRESS) {
            throw new IllegalStateException("대화를 종료할 수 없는 상태입니다.");
        }
        this.status = ChatStatus.COMPLETED;
    }
}
