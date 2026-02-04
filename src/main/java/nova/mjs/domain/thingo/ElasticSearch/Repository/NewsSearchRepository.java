package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Document.NewsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NewsSearchRepository extends ElasticsearchRepository<NewsDocument, String> {
}