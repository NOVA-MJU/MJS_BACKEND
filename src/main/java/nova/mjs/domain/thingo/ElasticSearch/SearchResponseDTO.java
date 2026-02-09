package nova.mjs.domain.thingo.ElasticSearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * SearchResponseDTO
 *
 * 목표
 * - 통합검색 응답 스키마를 고정한다.
 * - 도메인별 차이는 type으로 분기한다.
 * - 프론트에서 extra 없이 동일한 필드로 처리 가능하도록, 확장 필드는 nullable로 둔다.
 *
 * 정책(권장)
 * - highlightedTitle/highlightedContent는 "표시용 최종 텍스트"로 내려준다.
 *   (highlight가 없으면 원본으로 fallback 해서 null을 피하는 것이 프론트에 유리)
 *
 * 주의
 * - date는 Elasticsearch 저장 타입과 동일하게 Instant로 통일한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO {

    /** 통합 문서 ID (권장: TYPE:ORIGINAL_ID) */
    private String id;

    /** 표시용 제목(하이라이트 포함 가능, 없으면 원본으로 fallback 권장) */
    private String highlightedTitle;

    /** 표시용 본문(하이라이트 포함 가능, 없으면 원본으로 fallback 권장) */
    private String highlightedContent;

    /** 검색 정렬/표시 기준 시간 */
    private Instant date;

    private String link;
    private String category;
    private String type;
    private String imageUrl;

    /** ES score */
    private float score;

    /* 확장 필드(없으면 null) */
    private String authorName;
    private Integer likeCount;
    private Integer commentCount;
}
