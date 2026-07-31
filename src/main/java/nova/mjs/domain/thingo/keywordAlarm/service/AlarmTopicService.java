package nova.mjs.domain.thingo.keywordAlarm.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.keywordAlarm.dto.AlarmTopicDTO;
import nova.mjs.domain.thingo.semantic.TopicCatalog;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 알림 기능이 공통 Topic Catalog를 읽는 경계.
 * 실제 구독 저장·신규 공지 발송 정책은 이 서비스와 분리한다.
 */
@Service
@RequiredArgsConstructor
public class AlarmTopicService {

    private static final int MAX_LIMIT = 20;

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

    /** 상위 Topic 구독의 포함 범위를 계산한다. 사용자 필터는 이 결과와 별도로 적용해야 한다. */
    public Set<String> inclusiveTopicIds(String selectedTopicId) {
        return topicCatalog.inclusiveTopicIds(selectedTopicId);
    }
}
