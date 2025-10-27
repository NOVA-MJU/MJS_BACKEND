package nova.mjs.domain.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.community.DTO.BoardsQueryResult;
import nova.mjs.domain.community.DTO.CommunityBoardRequest;
import nova.mjs.domain.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.community.comment.repository.CommentRepository;
import nova.mjs.domain.community.entity.CommunityBoard;
import nova.mjs.domain.community.entity.enumList.CommunityCategory;
import nova.mjs.domain.community.exception.CommunityNotFoundException;
import nova.mjs.domain.community.likes.repository.CommunityLikeRepository;
import nova.mjs.domain.community.repository.CommunityBoardRepository;
import nova.mjs.domain.community.repository.projection.UuidCount;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.service.query.MemberQueryService;
import nova.mjs.domain.member.repository.MemberRepository;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3Service;
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
 *
 * 성능 최적화 포인트
 * 1) 댓글/좋아요 카운트 N+1 제거: 게시글 UUID 묶음으로 한 번에 집계
 * 2) 인기글을 DB에서 제외하고 페이지네이션: 전송량 절감 + 정확한 total 유지
 * 3) 불필요한 단건 count 쿼리 삭제: 맵으로 합치기
 *
 * 주의사항
 * - fetch join + pagination 조합은 JPA에서 카디널리티에 따라 페이징 틀어질 수 있으므로,
 *   @EntityGraph(attributePaths = "author")로 author만 로딩한 쿼리 사용
 * - 인기글 UUID 리스트가 비어있을 때 NOT IN () 방지 분기 필요
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CommunityBoardServiceImpl implements CommunityBoardService {

    private final CommunityBoardRepository communityBoardRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final MemberQueryService memberQueryService;
    private final S3Service s3Service;

    // S3 업로드 시 사용할 경로 prefix
    private final String boardPostPrefix = S3DomainType.COMMUNITY_POST.getPrefix();

    /**
     * 게시글 목록 조회 (페이지네이션 + 인기글 결합 + 좋아요 여부)
     *
     * communityCategoryRaw:
     * - "ALL" 이면 전체 카테고리
     * - 그 외면 CommunityCategory enum 값으로 파싱해서 해당 카테고리만 필터
     *
     * 반환되는 Page는:
     * - content: [인기글들(popular=true) ... , 일반글들(popular=false) ...]
     * - totalElements: "일반글" 기준 total (인기글은 헤더성 블록이라 total에 포함하지 않음)
     */
    @Override
    @Transactional(readOnly = false) // 여기서는 트랜잭션 없이 DTO 가공만 해도 되지만, loadBoards... 안에서 @Transactional(readOnly=true)로 DB I/O를 처리하므로 여기선 굳이 readOnly 줄 필요는 없음
    public Page<CommunityBoardResponse.SummaryDTO> getBoards(Pageable pageable, String email, String communityCategoryRaw) {

        // DB 왕복 + 첫 페이지 size 보정 + 카테고리 필터 반영
        BoardsQueryResult q = loadBoardsQueryResultWithFirstPageAdjustment(pageable, email, communityCategoryRaw);

        List<CommunityBoardResponse.SummaryDTO> popularDTOs =
                toSummaryDTOs(q.popularBoards(), q.likeCountMap(), q.commentCountMap(), q.likedUuids(), true);

        List<CommunityBoardResponse.SummaryDTO> generalDTOs =
                toSummaryDTOs(q.generalBoardsPage().getContent(), q.likeCountMap(), q.commentCountMap(), q.likedUuids(), false);

        // 1) 인기글(맨 위) + 일반글(그 뒤) 병합
        List<CommunityBoardResponse.SummaryDTO> merged = new ArrayList<>(popularDTOs.size() + generalDTOs.size());
        merged.addAll(popularDTOs);
        merged.addAll(generalDTOs);

        // 2) 정렬 규칙:
        //    - popular = true 가 항상 먼저
        //    - popular 내부 순서는 likeCount DESC 가 이미 반영된 상태이므로 건드리지 않음
        //    - 일반글 쪽은 createdAt 정렬 방향을 그대로 유지
        Comparator<CommunityBoardResponse.SummaryDTO> createdAtCmp =
                Comparator.comparing(CommunityBoardResponse.SummaryDTO::getCreatedAt);
        Sort.Order createdOrder = pageable.getSort().getOrderFor("createdAt");
        if (createdOrder != null && createdOrder.isDescending()) {
            createdAtCmp = createdAtCmp.reversed();
        }

        merged.sort(
                Comparator.<CommunityBoardResponse.SummaryDTO, Boolean>comparing(CommunityBoardResponse.SummaryDTO::isPopular)
                        .reversed() // popular=true 먼저
                        .thenComparing(dto -> dto.isPopular() ? 0 : 1) // popular 블록 유지용 보조 키
                        .thenComparing(createdAtCmp)
        );

        // 3) totalElements 계산
        //    - 고정 인기글은 헤더 성격으로 보고 totalElements는 일반글 total만 사용
        long totalElements = q.generalBoardsPage().getTotalElements();

        return new PageImpl<>(merged, pageable, totalElements);
    }

    /**
     * DB 왕복 전용 메서드
     *
     * 역할:
     * - (1) 인기글 top3 (카테고리별 or 전체)
     * - (2) 인기글을 제외한 일반글 페이지네이션 (카테고리별 or 전체)
     * - (3) 첫 페이지(page=0)일 때 일반글 size를 (요청 size - 인기글 수)로 보정해서
     *       "인기글 + 일반글 == 요청 size" 만족
     * - (4) 모든 글 UUID를 모아서 한 번에 좋아요수/댓글수/내가 좋아요 눌렀는지 여부까지 준비
     *
     * communityCategoryRaw:
     *   "ALL" 이면 categoryFilter = null
     *   그 외면 CommunityCategory로 파싱 시도
     */
    @Transactional(readOnly = true)
    protected BoardsQueryResult loadBoardsQueryResultWithFirstPageAdjustment(
            Pageable pageable,
            String email,
            String communityCategoryRaw
    ) {
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

        // 0) 카테고리 필터 결정
        // - "ALL" 또는 잘못된 값 -> categoryFilter = null (즉 전체 조회)
        // - 정상 enum 값 -> 그 카테고리만 조회
        CommunityCategory categoryFilter = null;
        if (communityCategoryRaw != null && !"ALL".equalsIgnoreCase(communityCategoryRaw)) {
            try {
                categoryFilter = CommunityCategory.valueOf(communityCategoryRaw.toUpperCase());
            } catch (IllegalArgumentException e) {
                categoryFilter = null;
            }
        }

        // 1) 인기글 top3 조회
        //    published=true && publishedAt>=twoWeeksAgo && likeCount DESC
        //    카테고리 필터가 있으면 해당 카테고리 내에서만 인기글 추출
        List<CommunityBoard> popularBoards = (categoryFilter == null)
                ? communityBoardRepository.findTop3PopularBoards(
                twoWeeksAgo,
                PageRequest.of(0, 3)
        )
                : communityBoardRepository.findTop3PopularBoardsByCategory(
                twoWeeksAgo,
                categoryFilter,
                PageRequest.of(0, 3)
        );

        List<UUID> popularUuids = popularBoards.stream()
                .map(CommunityBoard::getUuid)
                .toList();
        int popularCount = popularBoards.size();

        // 2) 일반글 페이지네이션
        // 첫 페이지면 size를 줄여서 "(인기글 n개) + (일반글 m개) = 요청 size"가 되도록 맞춘다
        Pageable generalPageable = pageable;
        if (pageable.getPageNumber() == 0 && popularCount > 0) {
            int adjustedSize = Math.max(0, pageable.getPageSize() - popularCount);
            generalPageable = PageRequest.of(0, adjustedSize, pageable.getSort());
        }

        Page<CommunityBoard> generalBoardsPage;
        if (categoryFilter == null) {
            // 전체 카테고리
            generalBoardsPage = popularUuids.isEmpty()
                    ? communityBoardRepository.findAllWithAuthor(generalPageable)
                    : communityBoardRepository.findAllWithAuthorExcluding(popularUuids, generalPageable);
        } else {
            // 특정 카테고리만
            generalBoardsPage = popularUuids.isEmpty()
                    ? communityBoardRepository.findAllWithAuthorByCategory(categoryFilter, generalPageable)
                    : communityBoardRepository.findAllWithAuthorByCategoryExcluding(categoryFilter, popularUuids, generalPageable);
        }

        // 3) like/댓글/좋아요 여부 집계
        //    인기글+일반글 전체 UUID를 모아 한 번에 가져온다.
        List<UUID> allUuids = Stream.concat(
                        popularBoards.stream(),
                        generalBoardsPage.getContent().stream()
                )
                .map(CommunityBoard::getUuid)
                .toList();

        // 로그인 사용자의 "좋아요 누른 글" 집합
        Set<UUID> likedUuids = Collections.emptySet();
        if (email != null && !allUuids.isEmpty()) {
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null) {
                likedUuids = new HashSet<>(
                        communityLikeRepository.findCommunityUuidsLikedByMember(member, allUuids)
                );
            }
        }

        // 좋아요 수 맵 (uuid -> cnt)
        Map<UUID, Long> likeCountMap = allUuids.isEmpty()
                ? Map.of()
                : communityLikeRepository.countLikesByBoardUuids(allUuids).stream()
                .collect(Collectors.toMap(UuidCount::getUuid, UuidCount::getCnt));

        // 댓글 수 맵 (uuid -> cnt)
        Map<UUID, Long> commentCountMap = allUuids.isEmpty()
                ? Map.of()
                : commentRepository.countCommentsByBoardUuids(allUuids).stream()
                .collect(Collectors.toMap(UuidCount::getUuid, UuidCount::getCnt));

        return new BoardsQueryResult(
                popularBoards,
                generalBoardsPage,
                likedUuids,
                likeCountMap,
                commentCountMap
        );
    }

    /** 엔티티 → DTO 변환 (기존 로직 유지) */
    private List<CommunityBoardResponse.SummaryDTO> toSummaryDTOs(
            List<CommunityBoard> boards,
            Map<UUID, Long> likeCountMap,
            Map<UUID, Long> commentCountMap,
            Set<UUID> likedUuids,
            boolean popular
    ) {
        return boards.stream()
                .map(b -> {
                    long likeCount = likeCountMap.getOrDefault(b.getUuid(), (long) b.getLikeCount());
                    long commentCount = commentCountMap.getOrDefault(b.getUuid(), 0L);
                    boolean isLiked = likedUuids.contains(b.getUuid());
                    return CommunityBoardResponse.SummaryDTO.fromEntityPreview(
                            b,
                            (int) likeCount,
                            (int) commentCount,
                            isLiked,
                            popular
                    );
                })
                .toList();
    }

    // 2. [게시글 상세 조회] (좋아요 여부 포함)
    @Override
    @Transactional(readOnly = true)
    public CommunityBoardResponse.DetailDTO getBoardDetail(UUID uuid, String email) {
        CommunityBoard board = getExistingBoard(uuid);

        int likeCount = communityLikeRepository.countByCommunityBoardUuid(uuid);
        int commentCount = commentRepository.countByCommunityBoardUuid(uuid);

        // 비로그인 사용자의 경우
        if (email == null) {
            return CommunityBoardResponse.DetailDTO.fromEntity(
                    board,
                    likeCount,
                    commentCount,
                    false,
                    false,
                    false
            );
        }

        // 로그인된 사용자
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            return CommunityBoardResponse.DetailDTO.fromEntity(
                    board,
                    likeCount,
                    commentCount,
                    false,
                    false,
                    false
            );
        }

        boolean isLiked = communityLikeRepository.findByMemberAndCommunityBoard(member, board).isPresent();
        boolean canEdit = canEdit(board, member);
        boolean canDelete = canDelete(board, member);

        log.debug("게시글 상세 조회 성공. uuid={}, likeCount={}, commentCount={}, isLiked={}, canEdit={}, canDelete={}",
                uuid, likeCount, commentCount, isLiked, canEdit, canDelete);

        return CommunityBoardResponse.DetailDTO.fromEntity(
                board,
                likeCount,
                commentCount,
                isLiked,
                canEdit,
                canDelete
        );
    }

    /*
     flow
     프론트에서 /upload-image로 업로드 요청 (백엔드는 S3에 static/images/posts/{uuid}/{filename}으로 업로드)
     프론트는 업로드된 이미지 URL을 content에 그대로 포함해서 전달
     우리는 content 자체만 저장하고 imageUrl은 별도 관리하지 않음
    */
    @Override
    @Transactional
    public CommunityBoardResponse.DetailDTO createBoard(CommunityBoardRequest request, String emailId) {
        log.info("[게시글 작성 요청] 사용자 이메일: {}", emailId);

        // 작성자 조회
        Member author = memberQueryService.getMemberByEmail(emailId);

        // published 값이 명시 안 되면 기본 true
        boolean published = request.getPublished() == null || request.getPublished();

        // body에서 넘어온 카테고리. 없으면 기본 FREE
        CommunityCategory category = (request.getCommunityCategory() != null)
                ? request.getCommunityCategory()
                : CommunityCategory.FREE;

        // 게시글 엔티티 생성
        CommunityBoard board = CommunityBoard.create(
                request.getTitle(),
                request.getContent(),
                request.getContentPreview(),
                category,
                published,
                author
        );

        communityBoardRepository.save(board);
        log.info("[게시글 저장 완료] UUID: {}", board.getUuid());

        int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
        int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());

        log.info("[게시글 작성 완료] UUID: {}, 좋아요: {}, 댓글: {}", board.getUuid(), likeCount, commentCount);

        return CommunityBoardResponse.DetailDTO.fromEntity(
                board,
                likeCount,
                commentCount,
                false // 방금 작성 직후엔 '좋아요 누름' 상태로 보지 않는다
        );
    }

    @Override
    @Transactional
    public CommunityBoardResponse.DetailDTO updateBoard(UUID boardUuid, CommunityBoardRequest request, String emailId) {

        // 1. 기존 게시글 조회
        CommunityBoard board = getExistingBoard(boardUuid);

        // 2. 사용자 권한 확인
        Member member = memberQueryService.getMemberByEmail(emailId);
        if (!board.getAuthor().equals(member)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // 3. 게시글 수정
        // category까지 수정하고 싶다면 board.update(...) 시그니처에 category 인자를 추가하고 여기서 적용해주면 된다.
        board.update(
                request.getTitle(),
                request.getContent(),
                request.getContentPreview(),
                request.getPublished()
        );

        // 4. 응답 생성
        int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
        int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
        boolean isLiked = communityLikeRepository.findByMemberAndCommunityBoard(member, board).isPresent();

        return CommunityBoardResponse.DetailDTO.fromEntity(
                board,
                likeCount,
                commentCount,
                isLiked
        );
    }

    @Override
    @Transactional
    public void deleteBoard(UUID uuid, String emailId) {
        // 1) 게시글 조회
        CommunityBoard board = getExistingBoard(uuid);

        // 2) 권한 확인 (작성자 또는 관리자)
        Member member = memberQueryService.getMemberByEmail(emailId);

        if (!canDelete(board, member)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다. (작성자 또는 관리자만 삭제 가능)");
        }

        // 3) DB 삭제
        communityBoardRepository.delete(board);

        // 4) s3에서도 게시글 폴더 정리
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
     * (현재는 고도화된 getBoards 경로에서 쓰진 않지만, fallback 용도로 유지)
     */
    private Page<CommunityBoardResponse.SummaryDTO> mapBoardsWithoutLogin(Page<CommunityBoard> boardPage) {
        return boardPage.map(board -> {
            int likeCount = communityLikeRepository.countByCommunityBoardUuid(board.getUuid());
            int commentCount = commentRepository.countByCommunityBoardUuid(board.getUuid());
            // isLiked = false
            return CommunityBoardResponse.SummaryDTO.fromEntityPreview(
                    board,
                    likeCount,
                    commentCount,
                    false
            );
        });
    }

    private boolean canDelete(CommunityBoard board, Member member) {
        return Objects.equals(board.getAuthor(), member)
                || Member.Role.OPERATOR.equals(member.getRole()); // NPE-safe
    }

    private boolean canEdit(CommunityBoard board, Member member) {
        return Objects.equals(board.getAuthor(), member);       // NPE-safe
    }
}
