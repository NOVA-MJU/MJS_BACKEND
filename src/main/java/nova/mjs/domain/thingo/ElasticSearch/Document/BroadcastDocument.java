package nova.mjs.domain.thingo.ElasticSearch.Document;

import lombok.*;
import nova.mjs.domain.thingo.ElasticSearch.SearchType;
import nova.mjs.domain.thingo.broadcast.entity.Broadcast;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.ZoneId;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "broadcast_index")
public class BroadcastDocument implements SearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    /**
     * content를 playlist(재생목록)으로 사용
     */
    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant date;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Keyword)
    private String link;

    @Override
    public String getType() {
        return SearchType.BROADCAST.name();
    }

    @Override
    public Instant getInstant() {
        return date;
    }

    public static BroadcastDocument from(Broadcast broadcast) {
        return BroadcastDocument.builder()
                .id(String.valueOf(broadcast.getId()))
                .title(broadcast.getTitle())
                .content(broadcast.getPlaylistTitle())
                .imageUrl(broadcast.getThumbnailUrl())
                .date(broadcast.getPublishedAt().atZone(ZoneId.systemDefault()).toInstant())
                .link(broadcast.getUrl())
                .build();
    }
}
