package nova.mjs.notice.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface NoticeSearchRepository extends ElasticsearchRepository<NoticeSearchDocument, String> {

    List<NoticeSearchDocument> findByTitleContaining(String keyword);
}
