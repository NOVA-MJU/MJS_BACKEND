package nova.mjs.community.comment.service;

import nova.mjs.community.comment.DTO.CommentResponseDto;

import java.util.UUID;

/**
 * 댓글 변경 서비스 인터페이스
 * CQRS 패턴의 Command 부분을 담당
 */
public interface CommentCommandService {
    
    /**
     * 댓글 작성
     */
    CommentResponseDto.CommentSummaryDto createComment(UUID communityBoardUuid, String content, String email);
    
    /**
     * 댓글 삭제
     */
    void deleteCommentByUuid(UUID commentUuid, String email);
    
    /**
     * 대댓글 작성
     */
    CommentResponseDto.CommentSummaryDto createReply(UUID parentCommentUuid, String content, String email);
}