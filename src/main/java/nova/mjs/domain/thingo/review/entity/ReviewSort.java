package nova.mjs.domain.thingo.review.entity;

import lombok.Getter;
import nova.mjs.domain.thingo.review.exception.ReviewValidationException;
import nova.mjs.util.exception.ErrorCode;

import java.util.Arrays;

/**
 * 리뷰 목록 정렬 기준.
 *
 * 외부 API 값은 소문자(latest/likes)로 고정하고, 내부 enum 이름이 바뀌더라도
 * 프론트 계약이 영향을 받지 않도록 별도 apiValue를 둔다.
 */
@Getter
public enum ReviewSort {

    LATEST("latest"),
    LIKES("likes");

    private final String apiValue;

    ReviewSort(String apiValue) {
        this.apiValue = apiValue;
    }

    public static ReviewSort fromApiValue(String value) {
        return Arrays.stream(values())
                .filter(sort -> sort.apiValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new ReviewValidationException(ErrorCode.REVIEW_SORT_INVALID));
    }
}
