package nova.mjs.domain.thingo.member.service;

import nova.mjs.domain.thingo.block.repository.BlockRepository;
import nova.mjs.domain.thingo.community.comment.likes.repository.CommentLikeRepository;
import nova.mjs.domain.thingo.community.comment.repository.CommentRepository;
import nova.mjs.domain.thingo.community.likes.repository.CommunityLikeRepository;
import nova.mjs.domain.thingo.community.repository.CommunityBoardRepository;
import nova.mjs.domain.thingo.keywordAlarm.repository.KeywordSubscriptionRepository;
import nova.mjs.domain.thingo.map.repository.PinFavoriteRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private CommunityBoardRepository communityBoardRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private CommunityLikeRepository communityLikeRepository;
    @Mock private CommentLikeRepository commentLikeRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private PinFavoriteRepository pinFavoriteRepository;
    @Mock private KeywordSubscriptionRepository keywordSubscriptionRepository;
    @Mock private BlockRepository blockRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void returnsAllMyPageActivityCounts() {
        Member member = Member.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .email("user@mju.ac.kr")
                .nickname("띵고")
                .build();
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(communityBoardRepository.countByAuthor(member)).willReturn(2);
        given(commentRepository.countByMember(member)).willReturn(3);
        given(communityLikeRepository.countByMember(member)).willReturn(4);
        given(pinFavoriteRepository.countByMember(member)).willReturn(5L);
        given(keywordSubscriptionRepository.countByMember(member)).willReturn(6L);
        given(blockRepository.countByBlocker(member)).willReturn(7L);

        var result = profileService.getMyProfileSummary(member.getEmail());

        assertThat(result.getNickname()).isEqualTo("띵고");
        assertThat(result.getPostCount()).isEqualTo(2);
        assertThat(result.getCommentCount()).isEqualTo(3);
        assertThat(result.getLikedPostCount()).isEqualTo(4);
        assertThat(result.getMapFavoriteCount()).isEqualTo(5);
        assertThat(result.getKeywordAlarmCount()).isEqualTo(6);
        assertThat(result.getBlockedUserCount()).isEqualTo(7);
    }
}
