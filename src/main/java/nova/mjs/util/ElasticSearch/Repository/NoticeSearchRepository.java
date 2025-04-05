package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.NoticeDocument;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.HighlightParameters;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface NoticeSearchRepository extends ElasticsearchRepository<NoticeDocument, String> {
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
    List<NoticeDocument> searchByTitleOrContent(String keyword);

}