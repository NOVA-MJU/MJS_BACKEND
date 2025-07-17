package nova.mjs.util.ElasticSearch.Document;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchDocument {
    String getId();
    String getTitle();
    String getContent();
    String getType();
    LocalDateTime getDate();
    List<String> getSuggest();

    default String getCategory() {
        return null; // 필요 없는 경우 null 반환
    }

    default String getLink() {
        return null;
    }

    default String getImageUrl(){
        return null;
    }
}
