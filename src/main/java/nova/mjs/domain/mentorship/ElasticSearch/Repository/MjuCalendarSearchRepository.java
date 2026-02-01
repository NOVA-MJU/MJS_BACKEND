package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import nova.mjs.domain.mentorship.ElasticSearch.Document.MjuCalendarDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MjuCalendarSearchRepository extends ElasticsearchRepository<MjuCalendarDocument, String> {
}
