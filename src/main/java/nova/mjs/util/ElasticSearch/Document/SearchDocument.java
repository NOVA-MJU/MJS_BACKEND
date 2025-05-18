package nova.mjs.util.ElasticSearch.Document;

public interface SearchDocument {
    String getId();
    String getTitle();
    String getContent();
    String getType();
    String getDate();
    String getLink();

    default String getCategory() {
        return null; // 필요 없는 경우 null 반환
    }
}
