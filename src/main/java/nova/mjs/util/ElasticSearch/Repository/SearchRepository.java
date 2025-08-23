package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.SearchDocument;
import nova.mjs.util.ElasticSearch.SearchType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface SearchRepository {
    SearchHits<? extends SearchDocument> search(String keyword, SearchType type, String order, Pageable pageable);
}
