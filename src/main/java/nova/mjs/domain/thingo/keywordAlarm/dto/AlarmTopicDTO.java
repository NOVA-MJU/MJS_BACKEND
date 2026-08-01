package nova.mjs.domain.thingo.keywordAlarm.dto;

import java.util.List;

/** 알림 구독용 표준 Topic 자동완성 응답. */
public class AlarmTopicDTO {

    private AlarmTopicDTO() {
    }

    public record Item(
            String topicId,
            String displayName,
            String description,
            String type
    ) {
    }

    public record AutocompleteResponse(String query, List<Item> items) {
    }

    /** 화면에는 짧은 keyword를 표시하고, 등록 시에는 topicId를 함께 전송한다. */
    public record RecommendedItem(
            String keyword,
            String topicId,
            String displayName,
            String description,
            String type
    ) {
    }

    public record RecommendedResponse(List<RecommendedItem> items) {
    }
}
