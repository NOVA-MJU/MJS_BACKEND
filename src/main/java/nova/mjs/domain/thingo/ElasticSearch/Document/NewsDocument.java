package nova.mjs.domain.thingo.ElasticSearch.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.news.entity.News;
import nova.mjs.domain.thingo.ElasticSearch.SearchType;
import nova.mjs.config.elasticsearch.KomoranTokenizerUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
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
    // 편집자 명
    private String authorName;

    @Override
    public String getType() {
        return SearchType.NEWS.name();
    }

     @Override
    public Instant getInstant() {
        return date;
    }

    @Override
    public String getImageUrl() {
        return this.imageUrl;
    }

    @Override
    public String getAuthorName() {
        return authorName;
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
                .authorName(news.getReporter())
                .build();
    }
}
