package nova.mjs.domain.thingo.keywordAlarm.event;

import java.util.UUID;

/** 리뷰 좋아요 트랜잭션이 정상 커밋된 뒤 FCM으로 전달할 최소 데이터. */
public record ReviewLikePushRequestedEvent(
        Long recipientMemberId,
        UUID reviewUuid,
        Long pinId,
        String body,
        String link
) {
}
