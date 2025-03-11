package nova.mjs.community.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.community.DTO.CommunityBoardRequest;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.entity.enumList.CommunityCategory;
import nova.mjs.community.exception.CommunityNotFoundException;
import nova.mjs.community.likes.repository.LikeRepository;
import nova.mjs.community.repository.CommunityBoardRepository;
import nova.mjs.member.Member;
import nova.mjs.member.MemberRepository;
import nova.mjs.member.exception.MemberNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class CommunityBoardService {

    private final CommunityBoardRepository communityBoardRepository;

    private final LikeRepository likeRepository;

    private final MemberRepository memberRepository;

    // 1. GET 페이지네이션
    public Page<CommunityBoardResponse> getBoards(Pageable pageable) {
        return communityBoardRepository.findAll(pageable)
                .map(board -> {
                    int likeCount = likeRepository.countByCommunityBoardUuid(board.getUuid());
                    return CommunityBoardResponse.fromEntity(board,likeCount);
                        });

    }

    // 2. GET 상세 content 조회
    public CommunityBoardResponse getBoardDetail(UUID uuid) {
        CommunityBoard board = getExistingBoard(uuid);
        int likeCount = likeRepository.countByCommunityBoardUuid(uuid);

        log.debug("자유 게시글 조회 성공. = {}, 좋아요 개수 = {}", uuid, likeCount);
        return CommunityBoardResponse.fromEntity(board, likeCount);
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
                request.getLikeCount(),
                author
        );
        communityBoardRepository.save(board);

        int likeCount = likeRepository.countByCommunityBoardUuid(board.getUuid());
        return CommunityBoardResponse.fromEntity(board, likeCount);
    }


    @Transactional
    public CommunityBoardResponse updateBoard(UUID uuid, CommunityBoardRequest request) {
        CommunityBoard board = getExistingBoard(uuid);

        // 게시글 업데이트
        board.update(
                request.getTitle(),
                request.getContent(),
                request.getPublished(),
                request.getContentImages() // contentImages 추가
        );
        int likeCount = likeRepository.countByCommunityBoardUuid(board.getUuid());
        // 엔티티를 DTO로 변환하여 반환
        return CommunityBoardResponse.fromEntity(board, likeCount);
    }



    // 5. DELETE 게시글 삭제
    @Transactional
    public void deleteBoard(UUID uuid) {
        CommunityBoard board = getExistingBoard(uuid);
        communityBoardRepository.delete(board);
    }


    private CommunityBoard getExistingBoard(UUID uuid) {
        return communityBoardRepository.findByUuid(uuid)
                .orElseThrow(CommunityNotFoundException::new);
    }
}
