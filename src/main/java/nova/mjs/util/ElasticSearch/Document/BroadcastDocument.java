package nova.mjs.util.ElasticSearch.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.broadcast.entity.Broadcast;
import nova.mjs.util.ElasticSearch.SearchType;
import nova.mjs.util.ElasticSearch.config.KomoranTokenizerUtil;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Document(indexName = "broadcast_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastDocument implements SearchDocument{

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

    @Override
    public String getType() {
        return SearchType.BROADCAST.name();
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
}
