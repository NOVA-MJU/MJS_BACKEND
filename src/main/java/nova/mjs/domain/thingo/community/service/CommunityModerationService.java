package nova.mjs.domain.thingo.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.domain.thingo.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.thingo.community.DTO.CommunityKeywordDeletionResponse;
import nova.mjs.domain.thingo.community.comment.DTO.CommentResponseDto;
import nova.mjs.domain.thingo.community.comment.entity.Comment;
import nova.mjs.domain.thingo.community.comment.exception.CommentNotFoundException;
import nova.mjs.domain.thingo.community.comment.repository.CommentRepository;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;
import nova.mjs.domain.thingo.community.exception.CommunityNotFoundException;
import nova.mjs.domain.thingo.community.repository.CommunityBoardRepository;
import nova.mjs.domain.thingo.report.entity.ReportTargetType;
import nova.mjs.domain.thingo.report.service.ContentModerationPort;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 신고 기반 콘텐츠 숨김/복원 (L2).
 *
 * 역할
 * - 신고 도메인의 {@link ContentModerationPort} 구현: 임계 초과 대상 자동 숨김
 * - 운영자용 숨김 목록 조회 + 숨김 해제(복원)
 *
 * 도메인 규칙
 * - 신고 도메인은 이 클래스를 인터페이스(포트)로만 참조한다(엔티티 직접 결합 방지).
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CommunityModerationService implements ContentModerationPort {

    private final CommunityBoardRepository communityBoardRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;

    /** 게시글 첨부 이미지 S3 prefix (건별 삭제 시 폴더 정리에 사용). */
    private final String boardPostPrefix = S3DomainType.COMMUNITY_POST.getPrefix();

    /**
     * 신고 누적 임계 초과 대상 자동 숨김 (신고 트랜잭션 내에서 호출됨).
     * - 이미 숨김이거나 대상이 없으면 무시(멱등).
     * - REVIEW(명지도 리뷰)는 ReviewModerationService가 담당하므로 여기선 무시한다.
     */
    @Override
    @Transactional
    public void hideByReport(ReportTargetType targetType, UUID targetUuid) {
        switch (targetType) {
            case BOARD -> communityBoardRepository.findByUuid(targetUuid)
                    .ifPresent(CommunityBoard::hideByReport);
            case COMMENT -> commentRepository.findByUuid(targetUuid)
                    .ifPresent(Comment::hideByReport);
            case REVIEW -> { /* 리뷰는 ReviewModerationService(ContentModerationPort)가 처리 */ }
        }
    }

    /**
     * 자동 숨김된 게시글 목록 조회 (운영자 검토 큐).
     */
    @Transactional(readOnly = true)
    public List<CommunityBoardResponse.SummaryDTO> getHiddenBoards() {
        return communityBoardRepository.findByHiddenTrueOrderByCreatedAtDesc().stream()
                .map(board -> CommunityBoardResponse.SummaryDTO.fromEntityPreview(
                        board,
                        board.getLikeCount(),
                        board.getCommentCount(),
                        false,
                        false
                ))
                .toList();
    }

    /**
     * 자동 숨김된 댓글 목록 조회 (운영자 검토 큐).
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto.CommentSummaryDto> getHiddenComments() {
        return commentRepository.findByHiddenTrueOrderByCreatedAtDesc().stream()
                .map(comment -> CommentResponseDto.CommentSummaryDto.fromEntity(comment, false))
                .toList();
    }

    /**
     * 게시글 숨김 해제 (운영자 검토 후 정상 복원).
     */
    @Transactional
    public void restoreBoard(UUID boardUuid) {
        CommunityBoard board = communityBoardRepository.findByUuid(boardUuid)
                .orElseThrow(CommunityNotFoundException::new);
        board.restore();
        log.info("게시글 숨김 해제 - uuid: {}", boardUuid);
    }

    /**
     * 댓글 숨김 해제 (운영자 검토 후 정상 복원).
     */
    @Transactional
    public void restoreComment(UUID commentUuid) {
        Comment comment = commentRepository.findByUuid(commentUuid)
                .orElseThrow(CommentNotFoundException::new);
        comment.restore();
        log.info("댓글 숨김 해제 - uuid: {}", commentUuid);
    }

    // =========================
    // 키워드 일괄 삭제 (운영자 전용, L2)
    // =========================

    /**
     * 제목/본문에 키워드가 포함된 게시글 미리보기 (삭제 전 확인용).
     *
     * - 실제 삭제 없이 "무엇이 지워질지"만 반환한다. 운영자가 목록을 확인하고
     *   {@link #deleteBoardsByKeyword(String)} 로 삭제를 실행하는 2단계 흐름을 권장한다.
     */
    @Transactional(readOnly = true)
    public CommunityKeywordDeletionResponse previewBoardsByKeyword(String keyword) {
        String normalized = normalizeKeyword(keyword);
        List<CommunityBoard> targets =
                communityBoardRepository.findByKeywordInTitleOrContent(normalized);
        log.info("키워드 삭제 미리보기 - keyword: '{}', 매칭 게시글 수: {}", normalized, targets.size());
        return CommunityKeywordDeletionResponse.of(normalized, targets);
    }

    /**
     * 제목/본문에 키워드가 포함된 게시글을 모두 삭제한다.
     *
     * 삭제 방식(중요)
     * - 벌크 JPQL DELETE 를 쓰지 않고 건별로 {@code repository.delete(board)} 를 호출한다.
     *   그래야 (1) 좋아요/댓글 cascade 삭제, (2) {@code @PostRemove} 리스너의
     *   통합검색 인덱스(unified_search_index) 정리 이벤트, (3) S3 첨부 폴더 삭제가
     *   각 게시글마다 정상 수행된다.
     * - 하나의 트랜잭션으로 묶어 전부 삭제되거나 전부 롤백되게 한다.
     *
     * @return 삭제된 건수와 삭제된 게시글 요약(감사 로그/응답용)
     */
    @Transactional
    public CommunityKeywordDeletionResponse deleteBoardsByKeyword(String keyword) {
        String normalized = normalizeKeyword(keyword);
        List<CommunityBoard> targets =
                communityBoardRepository.findByKeywordInTitleOrContent(normalized);

        // 응답/로그용 스냅샷을 삭제 전에 미리 만든다(삭제 후에는 프록시 접근이 위험).
        CommunityKeywordDeletionResponse response =
                CommunityKeywordDeletionResponse.of(normalized, targets);

        for (CommunityBoard board : targets) {
            communityBoardRepository.delete(board);
            s3Service.deleteFolder(boardPostPrefix + board.getUuid() + "/");
        }

        log.warn("키워드 게시글 일괄 삭제 완료 - keyword: '{}', 삭제 건수: {}",
                normalized, response.getDeletedCount());
        return response;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("삭제할 키워드를 입력해야 합니다.");
        }
        return keyword.trim();
    }
}
