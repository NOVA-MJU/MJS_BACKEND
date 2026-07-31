package nova.mjs.domain.thingo.semantic;

import java.time.Instant;
import java.util.Set;

/** 검색 인덱싱과 향후 알림 매칭이 공유하는 공지 의미 분류 결과. */
public record NoticeSemanticMetadata(
        Set<String> directTopicIds,
        Set<String> expandedTopicIds,
        NoticeEventType eventType,
        Set<NoticeAudience> audiences,
        Set<NoticeCampus> campuses,
        Instant deadline,
        int classificationVersion,
        ClassificationSource classificationSource,
        double classificationConfidence
) {

    /** 기존 검색 인덱스 필드명과의 호환성을 유지한다. */
    public Set<String> topicIds() {
        return expandedTopicIds;
    }
}
