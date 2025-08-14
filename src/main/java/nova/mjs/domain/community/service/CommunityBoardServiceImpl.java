package nova.mjs.domain.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.community.DTO.BoardsQueryResult;
import nova.mjs.domain.community.comment.repository.CommentRepository;
import nova.mjs.domain.community.DTO.CommunityBoardRequest;
import nova.mjs.domain.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.community.entity.CommunityBoard;
import nova.mjs.domain.community.entity.enumList.CommunityCategory;
import nova.mjs.domain.community.exception.CommunityNotFoundException;
import nova.mjs.domain.community.likes.repository.CommunityLikeRepository;
import nova.mjs.domain.community.repository.CommunityBoardRepository;
import nova.mjs.domain.community.repository.projection.UuidCount;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.repository.MemberRepository;
import nova.mjs.domain.member.exception.MemberNotFoundException;
import nova.mjs.domain.member.service.query.MemberQueryService;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3Service;
import nova.mjs.util.s3.S3ServiceImpl;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
//    @Override
//    public Page<CommunityBoardResponse.SummaryDTO> getBoards(Pageable pageable, String email) {
//        // 1) 페이지네이션으로 게시글 목록 조회
//        Page<CommunityBoard> boardPage = communityBoardRepository.findAllWithAuthor(pageable);
//
//        // 2) 게시글이 없으면 빈 응답 바로 반환
//        if (boardPage.isEmpty()) {
//            // Page.empty(...)로 반환하거나, boardPage.map(...) 형태로 반환
//            return boardPage.map(board -> null);
//        }
//
//        // 3) 비로그인 사용자면 -> isLiked = false
//        if (email == null) {
//            return mapBoardsWithoutLogin(boardPage);
//        }
//
//        // 4) 로그인된 사용자 조회
//        Member member = memberRepository.findByEmail(email).orElse(null);
//        // 이메일은 있으나 DB에 없는 경우 -> isLiked = false
//        if (member == null) {
//            return mapBoardsWithoutLogin(boardPage);
//        }
//
//        // 5) 모든 게시글의 UUID 목록 추출
//        List<UUID> boardUuids = boardPage.stream()
//                .map(CommunityBoard::getUuid)
//                .toList();
//
//        // 6) 사용자가 좋아요한 게시글 UUID 조회
//        List<UUID> likedUuids = communityLikeRepository.findCommunityUuidsLikedByMember(member, boardUuids);
//        Set<UUID> likedSet = new HashSet<>(likedUuids);
//
//
//        // 7) 각 게시글을 DTO로 매핑하면서 isLiked 설정
//        return boardPage.map(board -> {
//            int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
//            int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
//            boolean isLiked = likedSet.contains(board.getUuid());
//
//            log.info("작성자 닉네임 = {}", board.getAuthor() != null ? board.getAuthor().getNickname() : "null");
//            return CommunityBoardResponse.SummaryDTO.fromEntityPreview(board, likeCount, commentCount, isLiked);
//        });
//    }

    /**
     * 커뮤니티 게시글 목록 조회 - 성능 개선 버전
     *
     * 핵심 개선점
     * 1) 댓글/좋아요 카운트 N+1 제거 → 게시글 UUID 묶음으로 한 번에 집계 쿼리
     * 2) 인기글을 DB에서 제외하고 페이지네이션 → 네트워크/전송량 절감 + 정확한 page.total 유지
     * 3) 불필요한 단건 count 쿼리 삭제 → 맵으로 합치기
     *
     * 주의사항
     * - fetch join + pagination 조합은 JPA에서 카디널리티에 따라 페이징 틀어질 수 있으므로, @EntityGraph로 author만 로딩
     * - 인기글 UUID 리스트가 비어있을 때 NOT IN () 에러 방지를 위해 분기 처리
     */
    // Service or Query Service 내

    @Override
// 트랜잭션 없음: 커넥션 점유 없이 DTO 가공만
    public Page<CommunityBoardResponse.SummaryDTO> getBoards(Pageable pageable, String email) {
        BoardsQueryResult q = loadBoardsQueryResultWithFirstPageAdjustment(pageable, email); // ← 여기서 DB 왕복 + 첫 페이지 사이즈 보정

        List<CommunityBoardResponse.SummaryDTO> popularDTOs =
                toSummaryDTOs(q.popularBoards(), q.likeCountMap(), q.commentCountMap(), q.likedUuids(), true);

        List<CommunityBoardResponse.SummaryDTO> generalDTOs =
                toSummaryDTOs(q.generalBoardsPage().getContent(), q.likeCountMap(), q.commentCountMap(), q.likedUuids(), false);

        // 1) 인기글(맨 위) + 일반글(그 뒤) 병합
        List<CommunityBoardResponse.SummaryDTO> merged = new ArrayList<>(popularDTOs.size() + generalDTOs.size());
        merged.addAll(popularDTOs);
        merged.addAll(generalDTOs);

        // 2) 최종 정렬 규칙:
        //    - popular = true 가 항상 먼저
        //    - popular 내부 정렬은 likeCount DESC가 이미 반영된 상태이므로 그대로 보존
        //    - 일반글/전체에 대해서는 요청한 createdAt 방향을 적용
        Comparator<CommunityBoardResponse.SummaryDTO> createdAtCmp =
                Comparator.comparing(CommunityBoardResponse.SummaryDTO::getCreatedAt);
        Sort.Order createdOrder = pageable.getSort().getOrderFor("createdAt");
        if (createdOrder != null && createdOrder.isDescending()) {
            createdAtCmp = createdAtCmp.reversed();
        }

        // popular 우선, 그 다음 createdAt 순서 — 단, popular 내부의 likeCount DESC는 toSummaryDTOs 이전 단계에서 이미 반영된 순서를 보존하고 싶으므로
        // stable sort 를 위해 popular 우선만 보장하고 createdAt은 popular=false 영역을 주로 정렬하게 설계.
        merged.sort(
                Comparator.<CommunityBoardResponse.SummaryDTO, Boolean>comparing(CommunityBoardResponse.SummaryDTO::isPopular)
                        .reversed() // popular=true 먼저
                        .thenComparing(dto -> dto.isPopular() ? 0 : 1) // popular 블록 보존용 보조 키(선택)
                        .thenComparing(createdAtCmp)
        );

        // 3) totalElements 계산:
        //    - 클라이언트 페이지네이션 표시는 보통 "일반글" 기준 합계가 직관적입니다.
        //    - 고정 인기글은 헤더 성격이므로 totalElements는 일반글 total로 유지.
        long totalElements = q.generalBoardsPage().getTotalElements();

        return new PageImpl<>(merged, pageable, totalElements);
    }

    /**
     * 짧은 트랜잭션: DB 왕복만 수행하고 필요한 자료를 전부 실체화해 묶어서 반환
     * 첫 페이지(page=0)일 때만, 일반글 page size를 (요청 size - 인기글 수)로 보정하여
     * "인기글 + 일반글 = 요청 size"를 보장한다.
     */
    @Transactional(readOnly = true)
    protected BoardsQueryResult loadBoardsQueryResultWithFirstPageAdjustment(Pageable pageable, String email) {
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

        // 1) 인기글 3건 조회 (likeCount DESC 고정)
        List<CommunityBoard> popularBoards =
                communityBoardRepository.findTop3PopularBoards(twoWeeksAgo, PageRequest.of(0, 3));
        List<UUID> popularUuids = popularBoards.stream().map(CommunityBoard::getUuid).toList();
        int popularCount = popularBoards.size();

        // 2) 일반글 페이지네이션
        //    - 첫 페이지면 일반글 size를 줄여서 (popular + general == requested size) 성립
        Pageable generalPageable = pageable;
        if (pageable.getPageNumber() == 0 && popularCount > 0) {
            int adjustedSize = Math.max(0, pageable.getPageSize() - popularCount);
            generalPageable = PageRequest.of(0, adjustedSize, pageable.getSort());
        }

        Page<CommunityBoard> generalBoardsPage = popularUuids.isEmpty()
                ? communityBoardRepository.findAllWithAuthor(generalPageable)
                : communityBoardRepository.findAllWithAuthorExcluding(popularUuids, generalPageable);

        // 3) like/댓글/좋아요 여부 집계 준비
        List<UUID> allUuids = Stream.concat(popularBoards.stream(), generalBoardsPage.getContent().stream())
                .map(CommunityBoard::getUuid)
                .toList();

        Set<UUID> likedUuids = Collections.emptySet();
        if (email != null && !allUuids.isEmpty()) {
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null) {
                likedUuids = new HashSet<>(communityLikeRepository.findCommunityUuidsLikedByMember(member, allUuids));
            }
        }

        Map<UUID, Long> likeCountMap = allUuids.isEmpty() ? Map.of()
                : communityLikeRepository.countLikesByBoardUuids(allUuids).stream()
                .collect(Collectors.toMap(UuidCount::getUuid, UuidCount::getCnt));

        Map<UUID, Long> commentCountMap = allUuids.isEmpty() ? Map.of()
                : commentRepository.countCommentsByBoardUuids(allUuids).stream()
                .collect(Collectors.toMap(UuidCount::getUuid, UuidCount::getCnt));

        return new BoardsQueryResult(popularBoards, generalBoardsPage, likedUuids, likeCountMap, commentCountMap);
    }

    /** 엔티티 → DTO 변환 (기존 로직 유지) */
    private List<CommunityBoardResponse.SummaryDTO> toSummaryDTOs(
            List<CommunityBoard> boards,
            Map<UUID, Long> likeCountMap,
            Map<UUID, Long> commentCountMap,
            Set<UUID> likedUuids,
            boolean popular
    ) {
        return boards.stream().map(b -> {
            long likeCount = likeCountMap.getOrDefault(b.getUuid(), (long) b.getLikeCount());
            long commentCount = commentCountMap.getOrDefault(b.getUuid(), 0L);
            boolean isLiked = likedUuids.contains(b.getUuid());
            return CommunityBoardResponse.SummaryDTO.fromEntityPreview(b, (int) likeCount, (int) commentCount, isLiked, popular);
        }).toList();
    }


    // 2. [게시글 상세 조회] (좋아요 여부 포함)
    @Override
    @Transactional(readOnly = true)
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
    public CommunityBoardResponse.DetailDTO createBoard(CommunityBoardRequest request, String emailId) {
        log.info("[게시글 작성 요청] 사용자 이메일: {}", emailId);
    
        // 작성자 조회
        Member author = memberQueryService.getMemberByEmail(emailId);
        boolean published = request.getPublished() == null || request.getPublished();

        // 게시글 생성
        CommunityBoard board = CommunityBoard.create(
                request.getTitle(),
                request.getContent(),
                request.getContentPreview(),
                CommunityCategory.FREE,
                published,
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
        board.update(request.getTitle(), request.getContent(), request.getContentPreview(), request.getPublished());

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
