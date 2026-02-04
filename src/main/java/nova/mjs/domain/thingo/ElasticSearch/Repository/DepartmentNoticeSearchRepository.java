package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Document.DepartmentNoticeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DepartmentNoticeSearchRepository extends ElasticsearchRepository<DepartmentNoticeDocument, String> {
}
