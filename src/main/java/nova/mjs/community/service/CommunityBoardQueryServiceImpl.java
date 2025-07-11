package nova.mjs.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.community.comment.repository.CommentRepository;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.exception.CommunityNotFoundException;
import nova.mjs.community.likes.repository.CommunityLikeRepository;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 커뮤니티 게시판 조회 서비스 구현체
 * CQRS 패턴의 Query 부분을 담당
 */
@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class CommunityBoardQueryServiceImpl implements CommunityBoardQueryService {

    private final CommunityBoardRepository communityBoardRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Override
    public Page<CommunityBoardResponse.SummaryDTO> getBoards(Pageable pageable, String email) {
        // 1) 페이지네이션으로 게시글 목록 조회
        Page<CommunityBoard> boardPage = communityBoardRepository.findAllWithAuthor(pageable);

        // 2) 게시글이 없으면 바로 반환
        if (boardPage.isEmpty()) {
            // Page.empty(...)로 반환하거나, boardPage.map(...) 형태로 반환
            return boardPage.map(board -> null);
        }

        // 3) 비로그인 사용자면 -> isLiked = false
        if (email == null) {
            return boardPage.map(board -> {
                int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                // isLiked = false
                return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, false);
            });
        }

        // 4) 로그인된 사용자 조회
        Member member = memberRepository.findByEmail(email).orElse(null);
        // 이메일은 있으나 DB에 없는 경우 -> isLiked = false
        if (member == null) {
            return boardPage.map(board -> {
                int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, false);
            });
        }

        // 5) 모든 게시글의 UUID 목록 추출
        List<UUID> boardUuids = boardPage.stream()
                .map(CommunityBoard::getUuid)
                .toList();

        // 6) 사용자가 좋아요한 게시글 UUID 조회
        List<UUID> likedUuids = communityLikeRepository.findCommunityUuidsLikedByMember(member, boardUuids);
        Set<UUID> likedSet = new HashSet<>(likedUuids);

        // 7) 각 게시글을 DTO로 매핑하면서 isLiked 설정
        return boardPage.map(board -> {
            int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
            int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
            boolean isLiked = likedSet.contains(board.getUuid());

            log.info("작성자 닉네임 = {}", board.getAuthor() != null ? board.getAuthor().getNickname() : "null");
            return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, isLiked);

        });
    }

    @Override
    public CommunityBoardResponse.DetailDTO getBoardDetail(UUID uuid, String email) {
        CommunityBoard board = getExistingBoard(uuid);
        int likeCount = communityLikeRepository.countByCommunityBoardUuid(uuid);

        int commentCount = commentRepository.countByCommunityBoardUuid(uuid); // 댓글 개수 조회

        // 1) 비로그인 -> isLiked = false
        if (email == null) {
            return CommunityBoardResponse.DetailDTO.fromEntity(board, likeCount, commentCount, false);
        }

        // 2) 로그인된 사용자 찾기
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            return CommunityBoardResponse.DetailDTO.fromEntity(board, likeCount, commentCount, false);
        }

        // 3) 좋아요 여부 확인
        boolean isLiked = communityLikeRepository
                .findByMemberAndCommunityBoard(member, board)
                .isPresent();

        log.debug("자유 게시글 조회 성공. = {}, 좋아요 개수 = {}, 댓글 개수 = {}, 좋아요 = {}", uuid, likeCount, commentCount, isLiked);
        return CommunityBoardResponse.DetailDTO.fromEntity(board, likeCount, commentCount, isLiked);
    }

    private CommunityBoard getExistingBoard(UUID uuid) {
        return communityBoardRepository.findByUuid(uuid)
                .orElseThrow(CommunityNotFoundException::new);
    }
}