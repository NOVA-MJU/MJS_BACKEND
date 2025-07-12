package nova.mjs.domain.community.service;

import nova.mjs.domain.community.DTO.CommunityBoardRequest;
import nova.mjs.domain.community.DTO.CommunityBoardResponse;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 커뮤니티 게시판 서비스 인터페이스
 *
 * 게시판의 CRUD 및 조회 기능 정의
 *
 * - 비즈니스 로직에 대한 표준 인터페이스 제공
 * - 테스트, 확장, 대체 구현체 작성에 유리
 */

public interface CommunityBoardService {

    Page<CommunityBoardResponse.SummaryDTO> getBoards(Pageable pageable, String email);
    CommunityBoardResponse.DetailDTO getBoardDetail(UUID uuid, String email);
    CommunityBoardResponse.DetailDTO createBoard(CommunityBoardRequest request, String emailId);
    CommunityBoardResponse.DetailDTO updateBoard(UUID uuid, CommunityBoardRequest request, String email);
    void deleteBoard(UUID uuid, String email);
}
