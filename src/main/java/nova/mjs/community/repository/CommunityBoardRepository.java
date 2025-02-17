package nova.mjs.community.repository;

import nova.mjs.community.entity.CommunityBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBoardRepository extends JpaRepository<CommunityBoard, Long> {
    Optional<CommunityBoard> findByUuid(UUID uuid);

    // 댓글 lazy loading 해결을 위한 fetch join
    // 댓글을 한 번에 가져옴
    // 쿼리 최적화 (N + 1 문제 방지)로 성능을 향상시킬 수 있음
    @Query("SELECT cb from CommunityBoard cb JOIN fetch cb.comments where cb.uuid = :uuid")
    Optional<CommunityBoard> findByUuidWithComments(@Param("uuid") UUID uuid);
}
