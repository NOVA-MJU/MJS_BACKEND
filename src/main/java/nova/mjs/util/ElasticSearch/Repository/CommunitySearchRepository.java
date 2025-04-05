package nova.mjs.util.ElasticSearch.Repository;

import nova.mjs.util.ElasticSearch.Document.CommunityDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CommunitySearchRepository extends ElasticsearchRepository<CommunityDocument, String> {
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
    List<CommunityDocument> searchByTitleOrContent(String keyword);
}