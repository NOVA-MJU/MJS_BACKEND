package nova.mjs.domain.thingo.review.service.query;

import nova.mjs.domain.thingo.review.entity.ReviewSort;

import java.time.LocalDateTime;

/** 서버 내부에서만 사용하는 리뷰 무한 스크롤 위치. */
record ReviewCursor(
        ReviewSort sort,
        Integer likeCount,
        LocalDateTime createdAt,
        Long reviewId
) {
}
