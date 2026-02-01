package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import nova.mjs.domain.mentorship.ElasticSearch.Document.UnifiedSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UnifiedSearchRepository extends ElasticsearchRepository<UnifiedSearchDocument, String> {
}
