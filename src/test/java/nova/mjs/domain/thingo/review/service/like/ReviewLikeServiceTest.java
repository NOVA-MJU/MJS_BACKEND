package nova.mjs.domain.thingo.review.service.like;

import nova.mjs.domain.thingo.block.service.BlockQueryService;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.keywordAlarm.service.ActivityNotificationService;
import nova.mjs.domain.thingo.map.entity.Category;
import nova.mjs.domain.thingo.map.entity.CategoryGroup;
import nova.mjs.domain.thingo.map.entity.CategoryResultType;
import nova.mjs.domain.thingo.map.entity.Pin;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.service.query.MemberQueryService;
import nova.mjs.domain.thingo.report.entity.ReportTargetType;
import nova.mjs.domain.thingo.report.service.ReportQueryService;
import nova.mjs.domain.thingo.review.entity.Review;
import nova.mjs.domain.thingo.review.entity.ReviewKeyword;
import nova.mjs.domain.thingo.review.repository.ReviewLikeRepository;
import nova.mjs.domain.thingo.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewLikeServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewLikeRepository reviewLikeRepository;
    @Mock private MemberQueryService memberQueryService;
    @Mock private ActivityNotificationService activityNotificationService;
    @Mock private BlockQueryService blockQueryService;
    @Mock private ReportQueryService reportQueryService;

    @InjectMocks private ReviewLikeService service;

    @Test
    void 좋아요_등록_후_작성자_알림을_요청한다() {
        Member actor = member(1L, "좋아요맨");
        Member author = member(2L, "작성자");
        Review review = review(author);
        given(memberQueryService.getMemberByEmail("actor@mju.ac.kr")).willReturn(actor);
        given(reviewRepository.findByUuid(review.getUuid())).willReturn(Optional.of(review));
        given(blockQueryService.getHiddenMemberIds(1L)).willReturn(Set.of());
        given(reportQueryService.getSelfReportedTargetUuids(1L, ReportTargetType.REVIEW))
                .willReturn(Set.of());
        given(reviewLikeRepository.findByMemberAndReview(actor, review)).willReturn(Optional.empty());
        given(reviewLikeRepository.findTop2ByReviewOrderByIdDesc(review)).willReturn(List.of());
        given(reviewRepository.findLikeCount(review.getUuid())).willReturn(1);

        var result = service.toggleLike(review.getUuid(), "actor@mju.ac.kr");

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(1);
        verify(reviewRepository).increaseLikeCount(review.getUuid());
        verify(activityNotificationService).notifyReviewLike(review, actor, List.of(), 1);
    }

    @Test
    void 본인_리뷰에도_좋아요를_누를_수_있다() {
        // given: 작성자가 자기 리뷰에 좋아요를 누르는 상황
        Member author = member(1L, "작성자");
        Review review = review(author);
        given(memberQueryService.getMemberByEmail("author@mju.ac.kr")).willReturn(author);
        given(reviewRepository.findByUuid(review.getUuid())).willReturn(Optional.of(review));
        given(blockQueryService.getHiddenMemberIds(1L)).willReturn(Set.of());
        given(reportQueryService.getSelfReportedTargetUuids(1L, ReportTargetType.REVIEW))
                .willReturn(Set.of());
        given(reviewLikeRepository.findByMemberAndReview(author, review)).willReturn(Optional.empty());
        given(reviewLikeRepository.findTop2ByReviewOrderByIdDesc(review)).willReturn(List.of());
        given(reviewRepository.findLikeCount(review.getUuid())).willReturn(1);

        // when
        var result = service.toggleLike(review.getUuid(), "author@mju.ac.kr");

        // then: 좋아요는 반영되고, 자기 알림 제외는 ActivityNotificationService가 처리한다
        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(1);
        verify(reviewLikeRepository).save(any());
        verify(reviewRepository).increaseLikeCount(review.getUuid());
    }

    private Member member(long id, String nickname) {
        return Member.builder()
                .id(id).uuid(UUID.randomUUID()).role(Member.Role.USER).name(nickname)
                .nickname(nickname).email(nickname + "@mju.ac.kr").password("p")
                .college(College.AI_SOFTWARE).build();
    }

    private Review review(Member author) {
        CategoryGroup group = CategoryGroup.of("food", "식사", 1);
        Category category = Category.ofChip("restaurant", group, "음식점", null, null,
                null, CategoryResultType.PLACE_LIST, false, 1);
        Pin pin = Pin.ofExternalPlace("p", category, "장소", 37.0, 127.0,
                null, null, "주소");
        ReflectionTestUtils.setField(pin, "id", 10L);
        return Review.create(pin, author, "맛있어요", Set.of(ReviewKeyword.TASTY));
    }
}
