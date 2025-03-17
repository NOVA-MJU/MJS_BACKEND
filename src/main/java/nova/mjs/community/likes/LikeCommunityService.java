package nova.mjs.community.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.exception.CommunityNotFoundException;
import nova.mjs.community.likes.entity.LikeCommunity;
import nova.mjs.community.likes.repository.LikeCommunityRepository;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import nova.mjs.member.exception.MemberNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class LikeCommunityService {
    private final LikeCommunityRepository likeCommunityRepository;
    private final MemberRepository memberRepository;
    private final CommunityBoardRepository communityBoardRepository;

    // 1. 좋아요 추가 및 삭제 (토글 방식)
    @Transactional
    public boolean toggleLike(UUID boardUUID, String emailId) { // memberUUID를 제거하고 memberemail로 로직변경 - 이유는 회원 토큰안에 이메일 아이디가 담겨있기 때문에.
        Member member = memberRepository.findByEmail(emailId)
                .orElseThrow(MemberNotFoundException::new);
        CommunityBoard communityBoard = communityBoardRepository.findByUuid(boardUUID)
                .orElseThrow(CommunityNotFoundException::new);

        Optional<LikeCommunity> existingLike = likeCommunityRepository.findByMemberAndCommunityBoard(member, communityBoard);

        if (existingLike.isPresent()) {
            likeCommunityRepository.delete(existingLike.get());
            communityBoard.decreaseLikeCount();  // 좋아요 감소 메서드
            log.debug("좋아요 삭제 완료: member_emailId={}, boardUUID={}", emailId, boardUUID);
            return false; // 좋아요 취소됨
        } else {
            LikeCommunity likeCommunity = new LikeCommunity(member, communityBoard);
            likeCommunityRepository.save(likeCommunity);
            communityBoard.increaseLikeCount();  // 좋아요 증가 메서드
            log.debug("좋아요 추가 완료: member_emailId={}, boardUUID={}",emailId, boardUUID);
            return true; // 좋아요 추가됨
        }
    }
}
