package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Document.CommunityDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CommunitySearchRepository extends ElasticsearchRepository<CommunityDocument, String> {
}