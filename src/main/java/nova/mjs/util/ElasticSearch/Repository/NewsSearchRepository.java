package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.NewsDocument;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.HighlightParameters;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface NewsSearchRepository extends ElasticsearchRepository<NewsDocument, String> {
    @Query("""
{
  "bool": {
    "should": [
      { "match": { "title": "?0" }},
      { "match": { "content": "?0" }}
    ],
    "minimum_should_match": 1
  }
}
""")
    List<NewsDocument> searchByTitleOrContent(String keyword);
}