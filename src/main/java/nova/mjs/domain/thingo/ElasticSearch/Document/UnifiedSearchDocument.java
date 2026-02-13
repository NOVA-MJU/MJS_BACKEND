package nova.mjs.domain.thingo.ElasticSearch.Document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;

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

    @Field(type = FieldType.Double)
    private Double popularity;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Keyword)
    private String authorName;

    /**
     * Completion Suggester 전용 필드
     *
     * - Spring Data Elasticsearch의 Completion 타입을 사용해야 한다.
     * - 이 필드는 Elasticsearch 매핑에서 "completion" 타입이어야 한다.
     */
    @CompletionField(maxInputLength = 50)
    private Completion suggest;

    /**
     * 검색형 자동완성(search-as-you-type)용
     *
     * - prefix 뿐 아니라 2gram/3gram 기반 "부분 단어/중간 단어" 매칭에 강하다.
     * - completion이 못 잡는 “특공대/원문 중간 단어” 케이스를 보강한다.
     */
    @Field(type = FieldType.Search_As_You_Type, name = "title_autocomplete")
    private String titleAutocomplete;
}
