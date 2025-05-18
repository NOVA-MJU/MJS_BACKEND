package nova.mjs.util.ElasticSearch.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.news.entity.News;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "news_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class NewsDocument implements SearchDocument {
    @Id
    private String id;

    private String title;

    private String content;

    private String date;

    private String link;

    private String category;

    private String type;

    @Override
    public String getType() {
        return "News";
    }

    public static NewsDocument from(News news) {
        return NewsDocument.builder()
                .id(news.getId().toString())
                .title(news.getTitle())
                .content(news.getSummary())
                .date(news.getDate())
                .link(news.getLink())
                .category(news.getCategory().name())
                .type("news")
                .build();
    }

}
