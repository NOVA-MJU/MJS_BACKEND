package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Document.NoticeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NoticeSearchRepository extends ElasticsearchRepository<NoticeDocument, String> {
}