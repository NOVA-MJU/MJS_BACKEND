package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Document.StudentCouncilNoticeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface StudentCouncilNoticeSearchRepository extends ElasticsearchRepository<StudentCouncilNoticeDocument, String> {
}
