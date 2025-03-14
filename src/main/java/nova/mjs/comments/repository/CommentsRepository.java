package nova.mjs.comments.repository;

import nova.mjs.comments.entity.Comments;
import nova.mjs.community.entity.CommunityBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {

    // 페이징 해제
    List<Comments> findByCommunityBoard(CommunityBoard communityBoard);
    Optional<Comments> findByUuid(UUID uuid);
}
