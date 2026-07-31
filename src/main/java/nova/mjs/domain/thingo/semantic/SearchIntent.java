package nova.mjs.domain.thingo.semantic;

/** 사용자가 Topic에 관해 찾으려는 정보의 형태. 알림 구독 단위로 사용하지 않는다. */
public enum SearchIntent {
    APPLICATION_METHOD,
    APPLICATION_PERIOD,
    RESULT_LOOKUP,
    ELIGIBILITY,
    GENERAL
}
