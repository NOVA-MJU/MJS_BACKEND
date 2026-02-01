package nova.mjs.domain.mentorship.ElasticSearch.Repository;

import nova.mjs.domain.mentorship.ElasticSearch.Document.SearchDocument;
import nova.mjs.domain.mentorship.ElasticSearch.SearchType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface SearchRepository {
    SearchHits<? extends SearchDocument> search(String keyword, SearchType type, String order, Pageable pageable);
}
