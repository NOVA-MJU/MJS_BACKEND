package nova.mjs.util.ElasticSearch.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.community.entity.CommunityBoard;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Document(indexName = "community_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityDocument implements SearchDocument {

    @Id
    private String id;

    private String title;

    private String content;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant date;

    private String link;

    @Override
    public String getType() {
        return "Community";
    }

    @Override
    public LocalDateTime getDate() {
        return date != null
                ? date.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
    }

    public static CommunityDocument from(CommunityBoard board) {
        return CommunityDocument.builder()
                .id(String.valueOf(board.getId()))
                .title(board.getTitle())
                .content(board.getContent())
                .date(board.getPublishedAt().atZone(ZoneId.systemDefault()).toInstant())
                .build();
    }
}
