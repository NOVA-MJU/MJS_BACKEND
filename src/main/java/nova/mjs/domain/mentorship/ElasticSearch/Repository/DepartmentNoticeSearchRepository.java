package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import nova.mjs.domain.mentorship.ElasticSearch.Document.DepartmentNoticeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DepartmentNoticeSearchRepository extends ElasticsearchRepository<DepartmentNoticeDocument, String> {
}
