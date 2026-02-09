package nova.mjs.domain.thingo.ElasticSearch.indexing.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.ElasticSearch.SearchType;
import nova.mjs.domain.thingo.ElasticSearch.indexing.mapper.UnifiedSearchMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexUpdateServiceImpl implements SearchIndexUpdateService {

    private static final IndexCoordinates UNIFIED_INDEX =
            IndexCoordinates.of("search_unified");

    private final ElasticsearchOperations operations;
    private final UnifiedSearchMapper unifiedSearchMapper;

    @Override
    public void updateCommunityCounts(UUID boardUuid, Integer likeCount, Integer commentCount) {
        Map<String, Object> partial = new HashMap<>();

        if (likeCount != null) {
            partial.put("likeCount", likeCount);
        }
        if (commentCount != null) {
            partial.put("commentCount", commentCount);
        }
        if (partial.isEmpty()) {
            return;
        }

        String unifiedId =
                unifiedSearchMapper.buildId(SearchType.COMMUNITY.name(), boardUuid.toString());

        updateById(UNIFIED_INDEX, unifiedId, partial);
    }


    /**
     * UnifiedSearchDocument에 대한 partial update
     *
     * - 반드시 _id 규칙(TYPE:ORIGINAL_ID)과 일치해야 함
     * - 실패 시 로그만 남기고 도메인 로직은 영향 없음
     */
    private void updateById(IndexCoordinates index, String id, Map<String, Object> partialDoc) {
        try {
            UpdateQuery query = UpdateQuery.builder(id)
                    .withDocument(Document.from(partialDoc))
                    .build();

            operations.update(query, index);

        } catch (Exception e) {
            log.error("[Elasticsearch][PartialUpdate] failed. index={}, id={}, partial={}",
                    index.getIndexName(), id, partialDoc, e);
        }
    }
}
