package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Document.BroadcastDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BroadcastSearchRepository extends ElasticsearchRepository<BroadcastDocument, String> {}
