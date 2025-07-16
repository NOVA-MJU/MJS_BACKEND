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

/**
 * Elasticsearch 색인을 위한 커뮤니티 문서 객체
 *
 * - 게시글이 실제 게시된 경우에만 publishedAt을 Instant로 변환하여 date 필드에 저장
 * - publishedAt이 null인 경우 색인 시점에서 null 방어
 */
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

    private String type;

    private String link;

    @Override
    public String getType() {
        return "Community";
    }

    /**
     * Elasticsearch 색인을 위한 날짜 변환
     * - 저장된 date(Instant)를 LocalDateTime으로 변환
     */
    @Override
    public LocalDateTime getDate() {
        return date != null
                ? date.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
    }

    /**
     * CommunityBoard 엔티티를 Elasticsearch 문서로 변환
     * - publishedAt이 null일 수 있으므로 atZone 호출 전 null 체크 필요
     */
    public static CommunityDocument from(CommunityBoard board) {
        return CommunityDocument.builder()
                .id(String.valueOf(board.getId()))
                .title(board.getTitle())
                .content(board.getContent())
                .date(board.getPublishedAt() != null
                        ? board.getPublishedAt().atZone(ZoneId.systemDefault()).toInstant()
                        : null)
                .type("community")
                .build();
    }
}
