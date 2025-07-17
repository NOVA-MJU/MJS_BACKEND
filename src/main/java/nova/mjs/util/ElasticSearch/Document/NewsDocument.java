package nova.mjs.util.ElasticSearch.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.news.entity.News;
import nova.mjs.util.ElasticSearch.SearchType;
import nova.mjs.util.ElasticSearch.config.KomoranTokenizerUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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

    private String imageUrl;

    @CompletionField
    private List<String> suggest;

    private String type;

    @Override
    public String getType() {
        return SearchType.NEWS.name();
    }

    @Override
    public LocalDateTime getDate() {
        return date != null
                ? date.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
    }

    @Override
    public String getImageUrl() {
        return this.imageUrl;
    }

    public static NewsDocument from(News news) {
        return NewsDocument.builder()
                .id(news.getId().toString())
                .title(news.getTitle())
                .content(news.getSummary())
                .date(news.getDate().atZone(ZoneId.systemDefault()).toInstant())
                .link(news.getLink())
                .imageUrl(news.getImageUrl())
                .category(news.getCategory().name())
                .suggest(KomoranTokenizerUtil.generateSuggestions(news.getTitle()))
                .type(SearchType.NEWS.name())
                .build();
    }
}
