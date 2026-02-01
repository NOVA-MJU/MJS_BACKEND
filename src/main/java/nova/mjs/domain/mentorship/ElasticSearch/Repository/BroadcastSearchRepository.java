package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import nova.mjs.domain.mentorship.ElasticSearch.Document.BroadcastDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BroadcastSearchRepository extends ElasticsearchRepository<BroadcastDocument, String> {}
