package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.DepartmentNoticeDocument;
import nova.mjs.util.ElasticSearch.Document.DepartmentScheduleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DepartmentScheduleSearchRepository extends ElasticsearchRepository<DepartmentScheduleDocument, String> {}