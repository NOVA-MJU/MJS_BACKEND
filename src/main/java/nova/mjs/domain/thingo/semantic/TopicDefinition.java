package nova.mjs.domain.thingo.semantic;

import java.util.List;

/** 검색과 알림이 공유하는 표준 공지 주제. */
public record TopicDefinition(
        String topicId,
        String parentTopicId,
        String displayName,
        String description,
        boolean searchable,
        boolean subscribable,
        boolean enabled,
        List<String> searchTerms
) {
}
