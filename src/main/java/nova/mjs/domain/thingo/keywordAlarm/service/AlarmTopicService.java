package nova.mjs.domain.thingo.keywordAlarm.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.keywordAlarm.dto.AlarmTopicDTO;
import nova.mjs.domain.thingo.semantic.TopicCatalog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 알림 기능이 공통 Topic Catalog를 읽는 경계.
 * 실제 구독 저장·신규 공지 발송 정책은 이 서비스와 분리한다.
 */
@Service
@RequiredArgsConstructor
public class AlarmTopicService {

    private static final int MAX_LIMIT = 20;
    private static final List<RecommendedTopic> RECOMMENDED_TOPICS = List.of(
            new RecommendedTopic("수강신청", "COURSE_REGISTRATION"),
            new RecommendedTopic("휴·복학", "LEAVE_RETURN"),
            new RecommendedTopic("기숙사", "DORMITORY"),
            new RecommendedTopic("졸업", "GRADUATION"),
            new RecommendedTopic("국가근로", "NATIONAL_WORK_STUDY"),
            new RecommendedTopic("해외", "GLOBAL_PROGRAM")
    );

    private final TopicCatalog topicCatalog;

    public AlarmTopicDTO.AutocompleteResponse autocomplete(String query, int requestedLimit) {
        String normalizedQuery = query == null ? "" : query.trim();
        int limit = Math.max(1, Math.min(requestedLimit, MAX_LIMIT));
        var items = topicCatalog.autocomplete(normalizedQuery, limit, true).stream()
                .map(topic -> new AlarmTopicDTO.Item(
                        topic.topicId(),
                        topic.displayName(),
                        topic.description(),
                        topicCatalog.isGroup(topic.topicId()) ? "GROUP" : "TOPIC"
                ))
                .toList();
        return new AlarmTopicDTO.AutocompleteResponse(normalizedQuery, items);
    }

    /** 추천 칩을 누르는 즉시 넓은 표준 Topic으로 구독할 수 있는 목록. */
    public AlarmTopicDTO.RecommendedResponse recommended() {
        var items = RECOMMENDED_TOPICS.stream()
                .map(recommendation -> {
                    var topic = topicCatalog.find(recommendation.topicId())
                            .filter(candidate -> candidate.enabled() && candidate.subscribable())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Invalid recommended alarm topic: " + recommendation.topicId()));
                    return new AlarmTopicDTO.RecommendedItem(
                            recommendation.keyword(),
                            topic.topicId(),
                            topic.displayName(),
                            topic.description(),
                            topicCatalog.isGroup(topic.topicId()) ? "GROUP" : "TOPIC"
                    );
                })
                .toList();
        return new AlarmTopicDTO.RecommendedResponse(items);
    }

    /** 상위 Topic 구독의 포함 범위를 계산한다. 사용자 필터는 이 결과와 별도로 적용해야 한다. */
    public Set<String> inclusiveTopicIds(String selectedTopicId) {
        return topicCatalog.inclusiveTopicIds(selectedTopicId);
    }

    private record RecommendedTopic(String keyword, String topicId) {
    }
}
