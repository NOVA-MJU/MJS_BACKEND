package nova.mjs.domain.thingo.review.service.query;

import nova.mjs.domain.thingo.review.entity.Review;
import nova.mjs.domain.thingo.review.entity.ReviewSort;
import nova.mjs.domain.thingo.review.exception.ReviewValidationException;
import nova.mjs.util.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 리뷰 커서를 URL-safe Base64 문자열로 직렬화한다.
 * 프론트는 값을 해석하거나 조합하지 않고 다음 요청에 그대로 전달해야 한다.
 */
final class ReviewCursorCodec {

    private static final String VERSION = "v1";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ReviewCursorCodec() {
    }

    static String encode(Review review, ReviewSort sort) {
        String raw = String.join("|",
                VERSION,
                sort.getApiValue(),
                String.valueOf(review.getLikeCount()),
                TIME_FORMAT.format(review.getCreatedAt()),
                String.valueOf(review.getId()));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static ReviewCursor decode(String encoded, ReviewSort expectedSort) {
        if (encoded == null || encoded.isBlank()) {
            return new ReviewCursor(expectedSort, null, null, null);
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", -1);
            if (parts.length != 5 || !VERSION.equals(parts[0])) {
                throw invalidCursor();
            }
            if (!expectedSort.getApiValue().equals(parts[1])) {
                throw invalidCursor();
            }
            return new ReviewCursor(
                    expectedSort,
                    Integer.valueOf(parts[2]),
                    LocalDateTime.parse(parts[3], TIME_FORMAT),
                    Long.valueOf(parts[4]));
        } catch (ReviewValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw invalidCursor();
        }
    }

    private static ReviewValidationException invalidCursor() {
        return new ReviewValidationException(ErrorCode.REVIEW_CURSOR_INVALID);
    }
}
