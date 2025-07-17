package nova.mjs.domain.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.community.comment.repository.CommentRepository;
import nova.mjs.domain.community.DTO.CommunityBoardRequest;
import nova.mjs.domain.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.community.entity.CommunityBoard;
import nova.mjs.domain.community.entity.enumList.CommunityCategory;
import nova.mjs.domain.community.exception.CommunityNotFoundException;
import nova.mjs.domain.community.likes.repository.CommunityLikeRepository;
import nova.mjs.domain.community.repository.CommunityBoardRepository;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.repository.MemberRepository;
import nova.mjs.domain.member.exception.MemberNotFoundException;
import nova.mjs.domain.member.service.query.MemberQueryService;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3Service;
import nova.mjs.util.s3.S3ServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 커뮤니티 게시판 서비스 구현체
 *
 * - 게시글 목록 조회
 * - 게시글 상세 조회
 * - 게시글 작성 (이미지 처리 포함)
 * - 게시글 수정 (이미지 처리 포함)
 * - 게시글 삭제 (S3 이미지 삭제 포함)
 */

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class CommunityBoardServiceImpl implements CommunityBoardService {

    private final CommunityBoardRepository communityBoardRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final MemberRepository memberRepository;
    private final MemberQueryService memberQueryService;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;

    // S3 업로드 시 사용할 경로 prefix
    private final String boardPostPrefix = S3DomainType.COMMUNITY_POST.getPrefix();


    /**
     * [게시글 목록 조회] (페이지네이션 + 좋아요 여부)
     *
     * @param pageable 페이징 정보
     * @param email 로그인 사용자 이메일 (null 가능)
     * @return 게시글 목록 (SummaryDTO)
     */
    @Override
    public Page<CommunityBoardResponse.SummaryDTO> getBoards(Pageable pageable, String email) {
        // 1) 페이지네이션으로 게시글 목록 조회
        Page<CommunityBoard> boardPage = communityBoardRepository.findAllWithAuthor(pageable);

        // 2) 게시글이 없으면 빈 응답 바로 반환
        if (boardPage.isEmpty()) {
            // Page.empty(...)로 반환하거나, boardPage.map(...) 형태로 반환
            return boardPage.map(board -> null);
        }

        // 3) 비로그인 사용자면 -> isLiked = false
        if (email == null) {
            return mapBoardsWithoutLogin(boardPage);
        }

        // 4) 로그인된 사용자 조회
        Member member = memberRepository.findByEmail(email).orElse(null);
        // 이메일은 있으나 DB에 없는 경우 -> isLiked = false
        if (member == null) {
            return mapBoardsWithoutLogin(boardPage);
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

    // 2. [게시글 상세 조회] (좋아요 여부 포함)
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

    // 3. POST 게시글 작성

/* flow
    프론트에서 /upload-image로 업로드 요청(백엔드는 S3에 static/images/posts/{uuid}/{filename}으로 업로드)
    백엔드에서는 업로드된 파일 url과 글과 함께 모두 content로 요청 후 글은 content로 관리 = imageurl 관리 따로 안함.
*/
    @Transactional
    public CommunityBoardResponse.DetailDTO createBoard(CommunityBoardRequest request, UUID boardUuid, String emailId) {
        log.info("[게시글 작성 요청] 사용자 이메일: {}", emailId);
    
        // 작성자 조회
        Member author = memberQueryService.getMemberByEmail(emailId);

        // 게시글 생성
        CommunityBoard board = CommunityBoard.create(
                boardUuid,
                request.getTitle(),
                request.getContent(),
                CommunityCategory.FREE,
                request.getPublished(),
                author
        );
        communityBoardRepository.save(board);
        log.info("[게시글 저장 완료] UUID: {}", board.getUuid());

        int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
        int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());

        log.info("[게시글 작성 완료] UUID: {}, 좋아요: {}, 댓글: {}", board.getUuid(), likeCount, commentCount);

        return CommunityBoardResponse.DetailDTO.fromEntity(board, likeCount, commentCount, false);
    }



    @Transactional
    public CommunityBoardResponse.DetailDTO updateBoard(UUID boardUuid, CommunityBoardRequest request, String emailId) {

        // 1. 게시글 존재 여부 확인
        CommunityBoard board = getExistingBoard(boardUuid);

        // 2. 사용자 존재 및 권한 확인
        Member member = memberQueryService.getMemberByEmail(emailId);

        if (!board.getAuthor().equals(member)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // 4. 게시글 업데이트
        board.update(request.getTitle(), request.getContent(), request.getPublished());

        // 5. 응답 생성
        int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
        int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
        boolean isLiked = communityLikeRepository.findByMemberAndCommunityBoard(member, board).isPresent();

        // 엔티티를 DTO로 변환하여 반환
        return CommunityBoardResponse.DetailDTO.fromEntity(board, likeCount, commentCount, isLiked);
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(UUID uuid, String emailId) {
        // 1) 게시글 조회
        CommunityBoard board = getExistingBoard(uuid);

        // 2) 비로그인 or email == null → 에러
        Member member = memberQueryService.getMemberByEmail(emailId);

        if (!board.getAuthor().equals(member)) {throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");}

        // 5) 삭제
        // 게시글 삭제 로직에 추가
        communityBoardRepository.delete(board);
        // s3에서도 삭제하기
        String postFolder = boardPostPrefix + board.getUuid() + "/";
        s3Service.deleteFolder(postFolder);

        log.debug("게시글 삭제 성공. ID = {}, 작성자: {}", uuid, emailId);
    }


    private CommunityBoard getExistingBoard(UUID uuid) {
        return communityBoardRepository.findByUuid(uuid)
                .orElseThrow(CommunityNotFoundException::new);
    }

    /**
     * 비로그인 사용자용 게시글 목록 매핑
     */
    private Page<CommunityBoardResponse.SummaryDTO> mapBoardsWithoutLogin(Page<CommunityBoard> boardPage) {
        return boardPage.map(board -> {
            int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
            int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
            // isLiked = false
            return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, false);
        });
    }
}
