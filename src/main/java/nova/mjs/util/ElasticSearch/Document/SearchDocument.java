package nova.mjs.util.ElasticSearch.Document;

import org.springframework.data.elasticsearch.annotations.CompletionField;

import java.time.LocalDateTime;

public interface SearchDocument {
    String getId();
    String getTitle();
    String getContent();
    String getType();
    LocalDateTime getDate();

    default String getCategory() {
        return null; // 필요 없는 경우 null 반환
    }

    default String getLink() {
        return null;
    }
}
