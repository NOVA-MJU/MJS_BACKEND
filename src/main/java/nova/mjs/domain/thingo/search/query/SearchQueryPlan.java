package nova.mjs.domain.thingo.search.query;

import java.util.List;
import nova.mjs.domain.thingo.semantic.NoticeEventType;
import nova.mjs.domain.thingo.semantic.SearchIntent;

/** PostgreSQL 검색 쿼리가 사용할 후보·랭킹·커버리지 계획. */
public record SearchQueryPlan(
        String originalQuery,
        String matchTsQuery,
        String rankTsQuery,
        String coverageTsQuery,
        List<String> topicIds,
        NoticeEventType eventType,
        SearchIntent searchIntent
) {
    public SearchQueryPlan(String originalQuery,
                           String matchTsQuery,
                           String rankTsQuery,
                           String coverageTsQuery,
                           List<String> topicIds) {
        this(originalQuery, matchTsQuery, rankTsQuery, coverageTsQuery, topicIds,
                NoticeEventType.UNKNOWN, SearchIntent.GENERAL);
    }

    public boolean hasTopic() {
        return topicIds != null && !topicIds.isEmpty();
    }
}
