package nova.mjs.domain.thingo.review.service.like;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.service.query.MemberQueryService;
import nova.mjs.domain.thingo.keywordAlarm.service.ActivityNotificationService;
import nova.mjs.domain.thingo.block.service.BlockQueryService;
import nova.mjs.domain.thingo.report.entity.ReportTargetType;
import nova.mjs.domain.thingo.report.service.ReportQueryService;
import nova.mjs.domain.thingo.review.dto.ReviewDTO;
import nova.mjs.domain.thingo.review.entity.Review;
import nova.mjs.domain.thingo.review.entity.ReviewLike;
import nova.mjs.domain.thingo.review.exception.ReviewNotFoundException;
import nova.mjs.domain.thingo.review.exception.ReviewValidationException;
import nova.mjs.domain.thingo.review.repository.ReviewLikeRepository;
import nova.mjs.domain.thingo.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nova.mjs.util.exception.ErrorCode;

import java.util.Optional;
import java.util.UUID;

/**
 * 리뷰 좋아요 토글. 게시판과 같은 원자적 집계 방식을 사용하며,
 * 좋아요 등록 시 인앱 알림과 커밋 후 FCM 푸시를 연동한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewLikeService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MemberQueryService memberQueryService;
    private final ActivityNotificationService activityNotificationService;
    private final BlockQueryService blockQueryService;
    private final ReportQueryService reportQueryService;

    /**
     * 좋아요 토글. 없으면 추가(+1), 있으면 취소(-1). 결과 상태와 최신 좋아요 수 반환.
     */
    @Transactional
    public ReviewDTO.Response.LikeResult toggleLike(UUID reviewUuid, String email) {
        Member member = memberQueryService.getMemberByEmail(email);
        Review review = reviewRepository.findByUuid(reviewUuid)
                .orElseThrow(ReviewNotFoundException::new);

        // 목록/상세에서 볼 수 없는 리뷰는 좋아요 API로도 상태를 추측할 수 없도록 404로 통일한다.
        var hiddenAuthors = blockQueryService.getHiddenMemberIds(member.getId());
        var selfReported = reportQueryService.getSelfReportedTargetUuids(
                member.getId(), ReportTargetType.REVIEW);
        if (review.isHidden()
                || (hiddenAuthors != null && hiddenAuthors.contains(review.getAuthor().getId()))
                || (selfReported != null && selfReported.contains(reviewUuid))) {
            throw new ReviewNotFoundException();
        }
        if (review.isAuthoredBy(member)) {
            throw new ReviewValidationException(ErrorCode.REVIEW_SELF_LIKE_NOT_ALLOWED);
        }

        Optional<ReviewLike> existing = reviewLikeRepository.findByMemberAndReview(member, review);
        boolean liked;
        if (existing.isPresent()) {
            // 이미 좋아요 → 취소
            reviewLikeRepository.delete(existing.get());
            reviewRepository.decreaseLikeCount(reviewUuid);
            liked = false;
        } else {
            // 좋아요 추가
            reviewLikeRepository.save(new ReviewLike(member, review));
            reviewRepository.increaseLikeCount(reviewUuid);
            liked = true;
        }

        int likeCount = reviewRepository.findLikeCount(reviewUuid);
        if (liked) {
            var latestActors = reviewLikeRepository.findTop2ByReviewOrderByIdDesc(review)
                    .stream()
                    .map(ReviewLike::getMember)
                    .toList();
            activityNotificationService.notifyReviewLike(
                    review, member, latestActors, likeCount);
        }
        return ReviewDTO.Response.LikeResult.builder()
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }
}
