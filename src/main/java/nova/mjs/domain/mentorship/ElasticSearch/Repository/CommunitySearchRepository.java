package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import nova.mjs.domain.mentorship.ElasticSearch.Document.CommunityDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CommunitySearchRepository extends ElasticsearchRepository<CommunityDocument, String> {
}