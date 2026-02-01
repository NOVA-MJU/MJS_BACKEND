package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import nova.mjs.domain.mentorship.ElasticSearch.Document.NoticeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NoticeSearchRepository extends ElasticsearchRepository<NoticeDocument, String> {
}