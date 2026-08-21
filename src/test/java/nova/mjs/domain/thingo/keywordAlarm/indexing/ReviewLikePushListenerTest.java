package nova.mjs.domain.thingo.keywordAlarm.indexing;

import nova.mjs.domain.thingo.keywordAlarm.entity.DevicePlatform;
import nova.mjs.domain.thingo.keywordAlarm.entity.DeviceToken;
import nova.mjs.domain.thingo.keywordAlarm.event.ReviewLikePushRequestedEvent;
import nova.mjs.domain.thingo.keywordAlarm.repository.DeviceTokenRepository;
import nova.mjs.domain.thingo.keywordAlarm.service.fcm.FcmDispatch;
import nova.mjs.domain.thingo.keywordAlarm.service.fcm.FcmSender;
import nova.mjs.domain.thingo.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewLikePushListenerTest {

    @Mock private DeviceTokenRepository deviceTokenRepository;
    @Mock private FcmSender fcmSender;
    @InjectMocks private ReviewLikePushListener listener;

    @Test
    void 등록된_모든_기기로_리뷰_딥링크_푸시를_보낸다() {
        Member recipient = Member.builder().id(1L).uuid(UUID.randomUUID()).build();
        given(deviceTokenRepository.findByMember_Id(1L)).willReturn(List.of(
                DeviceToken.of(recipient, "token-a", DevicePlatform.ANDROID),
                DeviceToken.of(recipient, "token-b", DevicePlatform.IOS)));
        UUID reviewUuid = UUID.randomUUID();

        listener.on(new ReviewLikePushRequestedEvent(
                1L, reviewUuid, 33L, "좋아요맨님이 리뷰를 좋아합니다.",
                "/reviews/" + reviewUuid));

        ArgumentCaptor<FcmDispatch> captor = ArgumentCaptor.forClass(FcmDispatch.class);
        verify(fcmSender).sendAll(captor.capture());
        FcmDispatch dispatch = captor.getValue();
        assertThat(dispatch.tokens()).containsExactly("token-a", "token-b");
        assertThat(dispatch.keywordStyle()).isFalse();
        assertThat(dispatch.data()).containsEntry("type", "REVIEW_LIKE")
                .containsEntry("reviewUuid", reviewUuid.toString())
                .containsEntry("pinId", "33");
    }
}
