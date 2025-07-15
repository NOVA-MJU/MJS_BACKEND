package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.BroadcastDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BroadcastSearchRepository extends ElasticsearchRepository<BroadcastDocument, String> {}
