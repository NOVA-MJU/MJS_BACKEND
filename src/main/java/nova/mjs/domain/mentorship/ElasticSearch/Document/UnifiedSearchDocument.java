package nova.mjs.domain.mentorship.ElasticSearch.Document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "search_unified") // 통합 인덱스명
public class UnifiedSearchDocument {

    @Id
    private String id;          // "NOTICE:{pk}" 같은 형태 추천 (충돌 방지)

    @Field(type = FieldType.Keyword)
    private String type;        // NOTICE / COMMUNITY / ...

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime date;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String link;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    // 운영 안정용
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Boolean)
    private Boolean active;

    // completion suggester를 쓸 거면 mapping에서 completion 타입으로 잡아야 함
    // Spring Data Elasticsearch에서 completion 설정은 설정/매핑으로 관리 권장
    @Field(type = FieldType.Keyword) // (임시) 실제로는 completion으로 매핑 권장
    private List<String> suggest;
}
