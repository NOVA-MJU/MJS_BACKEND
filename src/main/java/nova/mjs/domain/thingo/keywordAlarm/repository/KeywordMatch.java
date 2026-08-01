package nova.mjs.domain.thingo.keywordAlarm.repository;

/**
 * 신규 콘텐츠와 매칭된 구독 한 건.
 */
public record KeywordMatch(Long subscriptionId, Long memberId, String keyword, String topicId) {

    public KeywordMatch(Long subscriptionId, Long memberId, String keyword) {
        this(subscriptionId, memberId, keyword, null);
    }
}
