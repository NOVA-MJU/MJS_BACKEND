package nova.mjs.domain.thingo.keywordAlarm.exception;

import nova.mjs.util.exception.ErrorCode;

/** 존재하지 않거나 구독할 수 없는 표준 Topic 요청. */
public class InvalidAlarmTopicException extends KeywordAlarmException {

    public InvalidAlarmTopicException() {
        super(ErrorCode.ALARM_TOPIC_INVALID);
    }
}
