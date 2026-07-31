package nova.mjs.domain.thingo.semantic;

/** 명시적으로 확인된 공지 대상. 빈 값은 전체가 아니라 '미지정'을 뜻한다. */
public enum NoticeAudience {
    ALL,
    ENROLLED_STUDENT,
    NEW_STUDENT,
    TRANSFER_STUDENT,
    GRADUATION_CANDIDATE,
    GRADUATE
}
