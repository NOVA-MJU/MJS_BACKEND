package nova.mjs.domain.thingo.ElasticSearch.unified;

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

    /**
     * 도메인 SearchDocument → 통합 검색 문서
     *
     * - 시간 타입 변환 없음
     * - Instant 그대로 전달
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
                .active(true)
                .popularity(0.0)
                .build();
    }
}
