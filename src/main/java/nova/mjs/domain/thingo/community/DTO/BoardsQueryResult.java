package nova.mjs.domain.thingo.community.DTO;

import nova.mjs.domain.thingo.community.entity.CommunityBoard;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 커뮤니티 목록 화면을 그리기 위해 "DB에서 한 번에" 모아온 결과 묶음.
 * - 트랜잭션 안에서 전부 materialize 후 이 컨테이너로 반환 → 트랜잭션 종료/커넥션 반납
 * - 트랜잭션 밖에서는 이 데이터만 사용하여 DTO 변환/머지 수행(추가 쿼리 없음)
 */
public record BoardsQueryResult(
        List<CommunityBoard> popularBoards,
        Page<CommunityBoard> generalBoardsPage,
        Set<UUID> likedUuids,
        Map<UUID, Long> likeCountMap,
        Map<UUID, Long> commentCountMap
) {}