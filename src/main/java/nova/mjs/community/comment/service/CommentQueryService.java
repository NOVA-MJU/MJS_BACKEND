package nova.mjs.community.comment.service;

import nova.mjs.community.comment.DTO.CommentResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * 댓글 조회 서비스 인터페이스
 * CQRS 패턴의 Query 부분을 담당
 */
public interface CommentQueryService {
    
    /**
     * 게시글의 댓글 목록 조회
     */
    List<CommentResponseDto.CommentSummaryDto> getCommentsByBoard(UUID communityBoardUuid, String email);
}