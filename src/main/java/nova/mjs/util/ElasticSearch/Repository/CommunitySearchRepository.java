package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.CommunityDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CommunitySearchRepository extends ElasticsearchRepository<CommunityDocument, String> {
}