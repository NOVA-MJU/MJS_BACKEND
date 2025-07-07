package nova.mjs.community.service;

import nova.mjs.community.DTO.CommunityBoardRequest;
import nova.mjs.community.DTO.CommunityBoardResponse;

import java.util.UUID;

/**
 * 커뮤니티 게시판 변경 서비스 인터페이스
 * CQRS 패턴의 Command 부분을 담당
 */
public interface CommunityBoardCommandService {
    
    /**
     * 게시글 작성
     */
    CommunityBoardResponse.DetailDTO createBoard(CommunityBoardRequest request, String emailId);
    
    /**
     * 게시글 수정
     */
    CommunityBoardResponse.DetailDTO updateBoard(UUID uuid, CommunityBoardRequest request, String email);
    
    /**
     * 게시글 삭제
     */
    void deleteBoard(UUID uuid, String email);
}