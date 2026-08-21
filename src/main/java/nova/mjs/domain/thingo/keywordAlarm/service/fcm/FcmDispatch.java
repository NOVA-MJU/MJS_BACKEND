package nova.mjs.domain.thingo.keywordAlarm.service.fcm;

import java.util.Map;
import java.util.List;

/**
 * FCM 발송 단위.
 * 한 회원의 기기 토큰 묶음과 알림 표시/데이터 페이로드.
 */
public record FcmDispatch(
        List<String> tokens,
        String title,
        String body,
        Map<String, String> data,
        boolean keywordStyle
) {
    /** 기존 키워드 알림 호출 호환용 생성자. */
    public FcmDispatch(List<String> tokens, String title, String body, Map<String, String> data) {
        this(tokens, title, body, data, true);
    }

    /** 제목을 가공하지 않는 활동 알림(좋아요/댓글 등). */
    public static FcmDispatch activity(List<String> tokens, String title, String body,
                                       Map<String, String> data) {
        return new FcmDispatch(tokens, title, body, data, false);
    }
}
