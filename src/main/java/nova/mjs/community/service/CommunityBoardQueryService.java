package nova.mjs.community.service;

import nova.mjs.community.DTO.CommunityBoardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 커뮤니티 게시판 조회 서비스 인터페이스
 * CQRS 패턴의 Query 부분을 담당
 */
public interface CommunityBoardQueryService {
    
    /**
     * 게시글 목록 조회 (페이지네이션)
     */
    Page<CommunityBoardResponse.SummaryDTO> getBoards(Pageable pageable, String email);
    
    /**
     * 게시글 상세 조회
     */
    CommunityBoardResponse.DetailDTO getBoardDetail(UUID uuid, String email);
}