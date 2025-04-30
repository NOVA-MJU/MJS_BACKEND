package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.SearchDocument;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface SearchRepository {
    SearchHits<? extends SearchDocument> search(String keyword, String type, int page, int size);
}
