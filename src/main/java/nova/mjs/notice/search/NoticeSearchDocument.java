package nova.mjs.notice.search;

import lombok.*;
import nova.mjs.notice.entity.Notice;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "notice_index")
public class NoticeSearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori") // 한국어 분석기
    private String title;

    @Field(type = FieldType.Keyword)
    private String date;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String link;

    public static NoticeSearchDocument fromEntity(Notice notice) {
        return NoticeSearchDocument.builder()
                .id(String.valueOf(notice.getId()))
                .title(notice.getTitle())
                .date(notice.getDate())
                .category(notice.getCategory())
                .link(notice.getLink())
                .build();
    }
}
