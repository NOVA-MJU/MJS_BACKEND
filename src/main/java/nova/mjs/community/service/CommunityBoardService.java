package nova.mjs.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.comment.repository.CommentRepository;
import nova.mjs.community.DTO.CommunityBoardRequest;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.entity.enumList.CommunityCategory;
import nova.mjs.community.exception.CommunityNotFoundException;
import nova.mjs.community.likes.repository.CommunityLikeRepository;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import nova.mjs.member.exception.MemberNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class CommunityBoardService {

    private final CommunityBoardRepository communityBoardRepository;

    private final CommunityLikeRepository communityLikeRepository;

    private final MemberRepository memberRepository;

    private final CommentRepository commentRepository;

    // 1. GET 페이지네이션
    public Page<CommunityBoardResponse> getBoards(Pageable pageable, String email) {
        // 1) 페이지네이션으로 게시글 목록 조회
        Page<CommunityBoard> boardPage = communityBoardRepository.findAll(pageable);

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
                return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, false);
            });
        }

        // 4) 로그인된 사용자 조회
        Member member = memberRepository.findByEmail(email).orElse(null);
        // 이메일은 있으나 DB에 없는 경우 -> isLiked = false
        if (member == null) {
            return boardPage.map(board -> {
                int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
                int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
                return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, false);
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
            return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, isLiked);
        });
    }

    // 2. GET 상세 content 조회
    public CommunityBoardResponse getBoardDetail(UUID uuid, String email) {
        CommunityBoard board = getExistingBoard(uuid);
        int likeCount = communityLikeRepository.countByCommunityBoardUuid(uuid);

        int commentCount = commentRepository.countByCommunityBoardUuid(uuid); // 댓글 개수 조회

        // 1) 비로그인 -> isLiked = false
        if (email == null) {
            return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, false);
        }

        // 2) 로그인된 사용자 찾기
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, false);
        }

        // 3) 좋아요 여부 확인
        boolean isLiked = communityLikeRepository
                .findByMemberAndCommunityBoard(member, board)
                .isPresent();

        log.debug("자유 게시글 조회 성공. = {}, 좋아요 개수 = {}, 댓글 개수 = {}, 좋아요 = {}", uuid, likeCount, commentCount, isLiked);
        return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, isLiked);
    }

    // 3. POST 게시글 작성
    @Transactional
    public CommunityBoardResponse createBoard(CommunityBoardRequest request, String emailId) {
        Member author = memberRepository.findByEmail(emailId)
                .orElseThrow(MemberNotFoundException::new);

        CommunityBoard board = CommunityBoard.create(
                request.getTitle(),
                request.getContent(),
                CommunityCategory.FREE,
                request.getPublished(),
                request.getContentImages(), // 이미지 리스트 처리
                author
        );
        communityBoardRepository.save(board);

        int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
        int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid()); // 추가

        return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, false);
    }


    @Transactional
    public CommunityBoardResponse updateBoard(UUID uuid, CommunityBoardRequest request, String email) {
        CommunityBoard board = getExistingBoard(uuid);

        // 게시글 업데이트
        board.update(
                request.getTitle(),
                request.getContent(),
                request.getPublished(),
                request.getContentImages() // contentImages 추가
        );
        int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
        int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid()); // 추가
        boolean isLiked = false;
        if (email != null) {
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null) {
                isLiked = communityLikeRepository
                        .findByMemberAndCommunityBoard(member, board)
                        .isPresent();
            }
        }

        // 엔티티를 DTO로 변환하여 반환
        return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, isLiked);
    }


    // 게시글 삭제
    @Transactional
    public void deleteBoard(UUID uuid, String email) {
        // 1) 게시글 조회
        CommunityBoard board = getExistingBoard(uuid);

        // 2) 비로그인 or email == null → 에러
        if (email == null) {
            throw new IllegalArgumentException("로그인한 사용자만 삭제할 수 있습니다.");
        }

        // 3) Member 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 4) 게시글 작성자가 현재 사용자와 같은지 체크
        //    (추가로 관리자(ADMIN)면 통과시킬 수도 있음)
        if (!board.getAuthor().getEmail().equals(email)) {
            // 본인이 아님
            throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 5) 삭제
        communityBoardRepository.delete(board);
        log.debug("게시글 삭제 성공. ID = {}, 작성자: {}", uuid, email);
    }


    private CommunityBoard getExistingBoard(UUID uuid) {
        return communityBoardRepository.findByUuid(uuid)
                .orElseThrow(CommunityNotFoundException::new);
    }
}
