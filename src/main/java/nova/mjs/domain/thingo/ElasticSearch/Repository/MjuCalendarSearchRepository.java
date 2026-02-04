package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Document.MjuCalendarDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MjuCalendarSearchRepository extends ElasticsearchRepository<MjuCalendarDocument, String> {
}
