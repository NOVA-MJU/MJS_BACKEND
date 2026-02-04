package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Document.UnifiedSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * UnifiedSearchRepository
 *
 * - 역할: UnifiedSearchDocument의 저장/삭제
 * - 검색 로직은 절대 넣지 않는다
 * - EventListener / Sync 전용
 */
public interface UnifiedSearchRepository
        extends ElasticsearchRepository<UnifiedSearchDocument, String> {
}
