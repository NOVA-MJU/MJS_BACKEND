package nova.mjs.domain.thingo.keywordAlarm.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.keywordAlarm.entity.DeviceToken;
import nova.mjs.domain.thingo.keywordAlarm.event.ReviewLikePushRequestedEvent;
import nova.mjs.domain.thingo.keywordAlarm.repository.DeviceTokenRepository;
import nova.mjs.domain.thingo.keywordAlarm.service.fcm.FcmDispatch;
import nova.mjs.domain.thingo.keywordAlarm.service.fcm.FcmSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 좋아요가 DB에 정상 반영된 뒤 작성자의 등록 기기로 푸시를 보낸다.
 * FCM 실패는 좋아요 트랜잭션과 분리되며 FcmSender가 비동기로 처리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewLikePushListener {

    private static final String PUSH_TITLE = "내 리뷰에 새 좋아요가 달렸어요";
    private static final String TYPE = "REVIEW_LIKE";

    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmSender fcmSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ReviewLikePushRequestedEvent event) {
        try {
            List<String> tokens = deviceTokenRepository
                    .findByMember_Id(event.recipientMemberId())
                    .stream()
                    .map(DeviceToken::getFcmToken)
                    .toList();
            if (tokens.isEmpty()) {
                return;
            }

            Map<String, String> data = new HashMap<>();
            data.put("type", TYPE);
            data.put("reviewUuid", event.reviewUuid().toString());
            data.put("pinId", event.pinId().toString());
            data.put("link", event.link());
            fcmSender.sendAll(FcmDispatch.activity(tokens, PUSH_TITLE, event.body(), data));
        } catch (Exception e) {
            log.warn("[리뷰 좋아요 푸시] 발송 준비 실패 - reviewUuid={}", event.reviewUuid(), e);
        }
    }
}
