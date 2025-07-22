package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.DepartmentNoticeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DepartmentNoticeSearchRepository extends ElasticsearchRepository<DepartmentNoticeDocument, String> {
}
