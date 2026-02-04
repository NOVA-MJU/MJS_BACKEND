package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Document.DepartmentScheduleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DepartmentScheduleSearchRepository extends ElasticsearchRepository<DepartmentScheduleDocument, String> {}