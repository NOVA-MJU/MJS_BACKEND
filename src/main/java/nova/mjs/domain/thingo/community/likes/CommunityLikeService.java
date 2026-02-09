package nova.mjs.domain.thingo.community.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.thingo.ElasticSearch.indexing.update.SearchIndexUpdateService;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;
import nova.mjs.domain.thingo.community.exception.CommunityNotFoundException;
import nova.mjs.domain.thingo.community.likes.entity.CommunityLike;
import nova.mjs.domain.thingo.community.likes.repository.CommunityLikeRepository;
import nova.mjs.domain.thingo.community.repository.CommunityBoardRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.exception.MemberNotFoundException;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * CommunityLikeService
 *
 * 목표
 * - 좋아요 토글 시 likeCount를 엔티티 수정으로 올리지 않는다.
 * - DB에서 원자적으로 증가/감소시켜 동시성에 강하게 만든다.
 * - 이벤트 시점에 ES likeCount도 partial update로 동기화한다.
 */
@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class CommunityLikeService {

    private final CommunityLikeRepository communityLikeRepository;
    private final MemberRepository memberRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final SearchIndexUpdateService searchIndexUpdateService;

    @Transactional
    public boolean toggleLike(UUID boardUuid, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        CommunityBoard board = communityBoardRepository.findByUuid(boardUuid)
                .orElseThrow(CommunityNotFoundException::new);

        Optional<CommunityLike> existing = communityLikeRepository.findByMemberAndCommunityBoard(member, board);

        boolean liked;
        if (existing.isPresent()) {
            // 1) 좋아요 삭제
            communityLikeRepository.delete(existing.get());

            // 2) 집계 컬럼 원자 감소
            communityBoardRepository.decreaseLikeCount(boardUuid);

            liked = false;
        } else {
            // 1) 좋아요 추가
            communityLikeRepository.save(new CommunityLike(member, board));

            // 2) 집계 컬럼 원자 증가
            communityBoardRepository.increaseLikeCount(boardUuid);

            liked = true;
        }

        // 3) 최신 likeCount 조회 후 ES 반영 (동기화용 쿼리는 유지하는 편이 안전)
        int newLikeCount = communityBoardRepository.findLikeCount(boardUuid);
        searchIndexUpdateService.updateCommunityCounts(boardUuid, newLikeCount, null);

        log.debug("좋아요 토글 완료. boardUuid={}, memberEmail={}, liked={}, likeCount={}",
                boardUuid, email, liked, newLikeCount);

        return liked;
    }
}
