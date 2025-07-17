package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.MjuCalendarDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MjuCalendarSearchRepository extends ElasticsearchRepository<MjuCalendarDocument, String> {
}
