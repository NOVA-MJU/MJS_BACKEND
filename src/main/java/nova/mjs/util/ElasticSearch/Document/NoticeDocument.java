package nova.mjs.util.ElasticSearch.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nova.mjs.domain.notice.entity.Notice;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Document(indexName = "notice_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDocument implements SearchDocument{

    @Id
    private String id;

    private String title;

    private String content;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant date;

    private String link;

    private String category;

    @Override
    public String getType() {
        return "Notice";
    }

    @Override
    public LocalDateTime getDate() {
        return date != null
                ? date.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
    }

    public static NoticeDocument from(Notice notice) {
        return NoticeDocument.builder()
                .id(notice.getId().toString())
                .title(notice.getTitle())
                .content("") // Notice는 content 없으면 빈 문자열 처리
                .date(notice.getDate().atZone(ZoneId.systemDefault()).toInstant())
                .link(notice.getLink())
                .category(notice.getCategory())
                .build();
    }

}