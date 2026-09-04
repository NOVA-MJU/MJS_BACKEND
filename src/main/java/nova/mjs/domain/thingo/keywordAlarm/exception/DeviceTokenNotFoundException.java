package nova.mjs.domain.thingo.keywordAlarm.exception;

import nova.mjs.util.exception.ErrorCode;

/**
 * 발송 대상 회원에게 등록된 FCM 기기 토큰이 없을 때 발생.
 * (수동 발송 등 특정 회원에게 직접 푸시를 보내려는데 받을 기기가 없는 경우)
 */
public class DeviceTokenNotFoundException extends KeywordAlarmException {

    public DeviceTokenNotFoundException() {
        super(ErrorCode.DEVICE_TOKEN_NOT_FOUND);
    }
}
