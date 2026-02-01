package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import nova.mjs.domain.mentorship.ElasticSearch.Document.DepartmentScheduleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DepartmentScheduleSearchRepository extends ElasticsearchRepository<DepartmentScheduleDocument, String> {}