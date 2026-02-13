package nova.mjs.domain.thingo.ElasticSearch.indexing.mapper;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.ElasticSearch.Document.SearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.Document.UnifiedSearchDocument;
import nova.mjs.domain.thingo.ElasticSearch.suggest.UnifiedSuggestFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UnifiedSearchMapper {

    private final UnifiedSuggestFactory suggestFactory;

    /**
     * 통합 인덱스 ID 규칙
     * - TYPE:ORIGINAL_ID
     */
    public String buildId(SearchDocument doc) {
        return safe(doc.getType()) + ":" + safe(doc.getId());
    }

    public String buildId(String type, String originalId) {
        return safe(type) + ":" + safe(originalId);
    }

    /**
     * 도메인 SearchDocument → UnifiedSearchDocument 변환
     *
     * 중요:
     * - suggest는 도메인 문서에 두지 않고, 여기(통합 매퍼)에서만 생성한다.
     * - Completion 타입은 Spring Data Completion으로만 저장한다.
     */
    public UnifiedSearchDocument from(SearchDocument doc) {

        String title = doc.getTitle();
        String content = doc.getContent();

        return UnifiedSearchDocument.builder()
                .id(buildId(doc))
                .originalId(doc.getId())
                .type(doc.getType())
                .title(title)
                .titleAutocomplete(title)            // search_as_you_type 입력
                .content(content)
                .category(doc.getCategory())
                .link(doc.getLink())
                .imageUrl(doc.getImageUrl())
                .date(doc.getInstant())
                .active(true)
                .popularity(0.0d)
                .updatedAt(Instant.now())
                .authorName(doc.getAuthorName())
                .likeCount(doc.getLikeCount())
                .commentCount(doc.getCommentCount())
                .suggest(suggestFactory.create(doc)) // 핵심: suggestFactory만 사용
                .build();
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }
}
