package nova.mjs.domain.thingo.ElasticSearch.indexing.mapper;

import nova.mjs.domain.thingo.ElasticSearch.Document.SearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.Document.UnifiedSearchDocument;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UnifiedSearchMapper {

    /**
     * 통합 인덱스 ID 규칙
     * - TYPE:ORIGINAL_ID
     */
    public String buildId(SearchDocument doc) {
        return doc.getType() + ":" + doc.getId();
    }

    public String buildId(String type, String originalId) {
        return type + ":" + originalId;
    }

    /**
     * 도메인 SearchDocument → UnifiedSearchDocument 변환
     *
     * - Unified 인덱스는 파생 인덱스
     * - 없는 값은 null로 유지
     */
    public UnifiedSearchDocument from(SearchDocument doc) {
        return UnifiedSearchDocument.builder()
                .id(buildId(doc))
                .originalId(doc.getId())
                .type(doc.getType())
                .title(doc.getTitle())
                .content(doc.getContent())
                .category(doc.getCategory())
                .link(doc.getLink())
                .imageUrl(doc.getImageUrl())
                .date(doc.getInstant())
                .updatedAt(Instant.now())
                .likeCount(doc.getLikeCount())
                .commentCount(doc.getCommentCount())
                .authorName(doc.getAuthorName())
                .active(true)
                .popularity(0.0)
                .build();
    }
}
