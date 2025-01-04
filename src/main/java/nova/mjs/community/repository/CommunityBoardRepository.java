package nova.mjs.community.repository;

import nova.mjs.community.entity.CommunityBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBoardRepository extends JpaRepository<CommunityBoard, Long> {
    Optional<CommunityBoard> findByUuid(UUID uuid);
}
