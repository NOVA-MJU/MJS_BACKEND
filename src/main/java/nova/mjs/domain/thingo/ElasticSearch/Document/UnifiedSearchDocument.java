package nova.mjs.domain.thingo.ElasticSearch.Document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "search_unified")
public class UnifiedSearchDocument {

    @Id
    private String id;              // TYPE:ORIGINAL_ID

    @Field(type = FieldType.Keyword)
    private String originalId;      // 원 도메인 문서 id

    @Field(type = FieldType.Keyword)
    private String type;            // NOTICE / COMMUNITY / ...

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String link;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant date;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant updatedAt;

    @Field(type = FieldType.Boolean)
    private Boolean active;

    /** 인기도/권위 등 랭킹 신호 (있으면 강력해짐) */
    @Field(type = FieldType.Double)
    private Double popularity;
}
