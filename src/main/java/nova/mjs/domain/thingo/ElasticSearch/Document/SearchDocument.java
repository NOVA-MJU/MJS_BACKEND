package nova.mjs.domain.thingo.ElasticSearch.Document;

import java.time.Instant;
import java.util.List;

/**
 * SearchDocument
 *
 * - 도메인별 Elasticsearch Document의 공통 인터페이스
 * - Elasticsearch 저장/검색 기준 타입만 노출한다
 * - 표현(LocalDateTime) 타입은 절대 포함하지 않는다
 */
public interface SearchDocument {

    String getId();

    String getTitle();

    String getContent();

    String getType();

    /**
     * Elasticsearch 저장용 절대 시점
     * - 반드시 Instant
     */
    Instant getInstant();

    List<String> getSuggest();

    /* 선택 필드들 */

    default String getCategory() {
        return null;
    }

    default String getLink() {
        return null;
    }

    default String getImageUrl() {
        return null;
    }
}
