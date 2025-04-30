package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.NoticeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NoticeSearchRepository extends ElasticsearchRepository<NoticeDocument, String> {
}