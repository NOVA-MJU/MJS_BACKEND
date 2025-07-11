package nova.mjs.util.ElasticSearch.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.news.entity.News;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant date;

    private String link;

    private String category;

    private String type;

    @Override
    public String getType() {
        return "News";
    }

    @Override
    public LocalDateTime getDate() {
        return date != null
                ? date.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
    }

    public static NewsDocument from(News news) {
        return NewsDocument.builder()
                .id(news.getId().toString())
                .title(news.getTitle())
                .content(news.getSummary())
                .date(news.getDate().atZone(ZoneId.systemDefault()).toInstant())
                .link(news.getLink())
                .category(news.getCategory().name())
                .type("news")
                .build();
    }

}
