package nova.mjs.domain.thingo.keywordAlarm.exception;

import nova.mjs.util.exception.ErrorCode;

/**
 * 수동 발송 시 키워드에 매칭되는 과거 콘텐츠(통합검색 인덱스)를 찾지 못했을 때 발생.
 */
public class AlarmSourceNotFoundException extends KeywordAlarmException {

    public AlarmSourceNotFoundException() {
        super(ErrorCode.ALARM_SOURCE_NOT_FOUND);
    }
}
