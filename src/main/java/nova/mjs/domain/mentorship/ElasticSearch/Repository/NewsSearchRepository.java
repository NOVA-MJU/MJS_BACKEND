package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import nova.mjs.domain.mentorship.ElasticSearch.Document.NewsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NewsSearchRepository extends ElasticsearchRepository<NewsDocument, String> {
}