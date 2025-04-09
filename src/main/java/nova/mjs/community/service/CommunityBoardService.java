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
import nova.mjs.util.s3.S3Service;
import org.springframework.beans.factory.annotation.Value;
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
    private final S3Service s3Service;

    @Value("${s3.path.custom.board-temp}")
    private String boardTempPrefix;

    @Value("${s3.path.custom.board-post}")
    private String boardPostPrefix;



    // 1. GET 페이지네이션
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

            return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, isLiked);

            log.info("작성자 닉네임 = {}", board.getAuthor() != null ? board.getAuthor().getNickname() : "null");
            return CommunityBoardResponse.fromEntity(board, likeCount, commentCount, isLiked);

        });
    }

    // 2. GET 상세 content 조회
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
    프론트에서 이미지 선택
    백엔드 /upload-image로 업로드 요청
    백엔드는 S3에 posts/temp/{uuid}/{filename}으로 업로드
    백엔드는 업로드 성공 후, CloudFront URL을 만들어 응답함 → 예: https://d2zppxfma88m3u.cloudfront.net/posts/temp/uuid/filename.jpg
    게시글 작성 시 contentImages에 위 URL들을 포함하여 전달
    게시글 등록 성공 시 posts/temp → posts/{게시글UUID}/로 이동 (S3 내부에서 copy + delete)
*/

    @Transactional
    public CommunityBoardResponse.DetailDTO createBoard(CommunityBoardRequest request, String emailId) {
        log.info("[게시글 작성 요청] 사용자 이메일: {}", emailId);
    

        // 작성자 조회
        Member author = memberRepository.findByEmail(emailId)
                .orElseThrow(() -> {
                    log.warn("[작성자 조회 실패] 이메일: {}", emailId);
                    return new MemberNotFoundException();
                });

        // 게시글 생성
        CommunityBoard board = CommunityBoard.create(
                request.getTitle(),
                request.getContent(),
                CommunityCategory.FREE,
                request.getPublished(),
                request.getContentImages(),
                author
        );
        communityBoardRepository.save(board);
        log.info("[게시글 저장 완료] UUID: {}", board.getUuid());

        // 이미지 이동 처리 (temp → post)
        log.info("[이미지 이동 시작] 총 이미지 수: {}", request.getContentImages().size());

        List<String> tempImages = request.getContentImages().stream()
                .filter(url -> s3Service.extractKeyFromUrl(url).startsWith(boardTempPrefix))
                .toList();

        log.info("[temp 이미지 추출] 이동 대상 수: {}", tempImages.size());

        for (String tempImageUrl : tempImages) {
            String tempKey = s3Service.extractKeyFromUrl(tempImageUrl); // board/temp/{uuid}/파일명
            String fileName = tempKey.substring(tempKey.lastIndexOf('/') + 1);
            String realKey = boardPostPrefix + board.getUuid() + "/" + fileName;

            log.info("[이미지 복사] from: {}, to: {}", tempKey, realKey);
            s3Service.copyFile(tempKey, realKey);

//            log.info("[임시 이미지 삭제] key: {}", tempKey);
//            s3Service.deleteFile(tempKey);
        }

        log.info("[이미지 이동 완료] 게시글 UUID: {}", board.getUuid());

        int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
        int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());

        log.info("[게시글 작성 완료] UUID: {}, 좋아요: {}, 댓글: {}", board.getUuid(), likeCount, commentCount);

        return CommunityBoardResponse.DetailDTO.fromEntity(board, likeCount, commentCount, false);
    }



    @Transactional
    public CommunityBoardResponse.DetailDTO updateBoard(UUID uuid, CommunityBoardRequest request, String email) {

        // 1. 게시글 존재 여부 확인
        CommunityBoard board = getExistingBoard(uuid);

        // 2. 사용자 존재 및 권한 확인
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
        if (!board.getAuthor().equals(member)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // 3. 기존 vs 새로운 이미지 리스트 비교
        List<String> oldImages = board.getContentImages();
        List<String> newImages = request.getContentImages();

        // 3-1. 삭제 대상 이미지 제거
        List<String> toDelete = oldImages.stream()
                .filter(old -> !newImages.contains(old))
                .toList();
        toDelete.forEach(imageUrl -> {
            String key = s3Service.extractKeyFromUrl(imageUrl);
            s3Service.deleteFile(key);
        });

        // 3-2. 새로 추가된 이미지 중 temp 경로에 있는 것만 복사 후 삭제
        List<String> addedTempImages = newImages.stream()
                .filter(newImg -> !oldImages.contains(newImg))
                .filter(newImg -> s3Service.extractKeyFromUrl(newImg).startsWith(boardTempPrefix))
                .toList();

        for (String tempImageUrl : addedTempImages) {
            String tempKey = s3Service.extractKeyFromUrl(tempImageUrl); // board/temp/{tempUuid}/filename
            String filename = tempKey.substring(tempKey.lastIndexOf('/') + 1);
            String realKey = boardPostPrefix + uuid + "/" + filename;

            s3Service.copyFile(tempKey, realKey);
            s3Service.deleteFile(tempKey);
        }

        // 4. 게시글 업데이트
        board.update(
                request.getTitle(),
                request.getContent(),
                request.getPublished(),
                request.getContentImages()
        );

        // 5. 응답 생성
        int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
        int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
        boolean isLiked = communityLikeRepository
                .findByMemberAndCommunityBoard(member, board)
                .isPresent();


        // 엔티티를 DTO로 변환하여 반환
        return CommunityBoardResponse.DetailDTO.fromEntity(board, likeCount, commentCount, isLiked);
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
            throw new MemberNotFoundException();
        }

        // 5) 삭제
        // 게시글 삭제 로직에 추가
        communityBoardRepository.delete(board);
        // s3에서도 삭제하기
        String postFolder = boardPostPrefix + board.getUuid() + "/";
        s3Service.deleteFolder(postFolder);

        log.debug("게시글 삭제 성공. ID = {}, 작성자: {}", uuid, email);
    }


    private CommunityBoard getExistingBoard(UUID uuid) {
        return communityBoardRepository.findByUuid(uuid)
                .orElseThrow(CommunityNotFoundException::new);
    }
}
