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

    // 게시판 페이지네이션 조회
    Page<CommunityBoardResponse.SummaryDTO> getBoards(Pageable pageable, String email);
    // 게시판 상세 조회
    CommunityBoardResponse.DetailDTO getBoardDetail(UUID uuid, String email);
    // 게시판 생성
    CommunityBoardResponse.DetailDTO createBoard(CommunityBoardRequest request, String emailId);
    // 게시판 업데이트
    CommunityBoardResponse.DetailDTO updateBoard(UUID uuid, CommunityBoardRequest request, String email);
    // 게시판 삭제
    void deleteBoard(UUID uuid, String email);
}
