package nova.mjs.domain.thingo.ElasticSearch.Document;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.config.elasticsearch.KomoranTokenizerUtil;
import nova.mjs.domain.thingo.ElasticSearch.SearchType;
import nova.mjs.domain.thingo.broadcast.entity.Broadcast;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Document(indexName = "broadcast_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastDocument implements SearchDocument {

    @Id
    private String id;

    private String title;

    private String content; //content를 playlist(재생목록)으로 사용

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant date;

    private String imageUrl;

    private String link;

    @CompletionField
    private List<String> suggest;

    private String type;

    public static BroadcastDocumentBuilder builder() {
        return new BroadcastDocumentBuilder();
    }

    @Override
    public String getType() {
        return SearchType.BROADCAST.name();
    }

     @Override
    public Instant getInstant() {
        return date;
    }

    @Override
    public String getImageUrl() {
        return this.imageUrl;
    }


    public static BroadcastDocument from(Broadcast broadcast) {
        return BroadcastDocument.builder()
                .id(String.valueOf(broadcast.getId()))
                .title(broadcast.getTitle())
                .content(broadcast.getPlaylistTitle()) // playlistTitle을 content로 사용
                .imageUrl(broadcast.getThumbnailUrl())
                .date(broadcast.getPublishedAt().atZone(ZoneId.systemDefault()).toInstant())
                .link(broadcast.getUrl())
                .suggest(KomoranTokenizerUtil.generateSuggestions(broadcast.getTitle()))
                .type(SearchType.BROADCAST.name())
                .build();
    }

    public static class BroadcastDocumentBuilder {
        private String id;
        private String title;
        private String content;
        private Instant date;
        private String imageUrl;
        private String link;
        private List<String> suggest;
        private String type;

        BroadcastDocumentBuilder() {
        }

        public BroadcastDocumentBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BroadcastDocumentBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BroadcastDocumentBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BroadcastDocumentBuilder date(Instant date) {
            this.date = date;
            return this;
        }

        public BroadcastDocumentBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public BroadcastDocumentBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BroadcastDocumentBuilder suggest(List<String> suggest) {
            this.suggest = suggest;
            return this;
        }

        public BroadcastDocumentBuilder type(String type) {
            this.type = type;
            return this;
        }

        public BroadcastDocument build() {
            return new BroadcastDocument(this.id, this.title, this.content, this.date, this.imageUrl, this.link, this.suggest, this.type);
        }

        public String toString() {
            return "BroadcastDocument.BroadcastDocumentBuilder(id=" + this.id + ", title=" + this.title + ", content=" + this.content + ", date=" + this.date + ", imageUrl=" + this.imageUrl + ", link=" + this.link + ", suggest=" + this.suggest + ", type=" + this.type + ")";
        }
    }
}
