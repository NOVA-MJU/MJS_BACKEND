package nova.mjs.comments.repository;

import nova.mjs.comments.entity.Comments;
import nova.mjs.community.entity.CommunityBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {

    // ✅ 페이징을 적용하여 특정 게시글의 댓글을 조회
    Page<Comments> findByCommunityBoard(CommunityBoard communityBoard, Pageable pageable);
    Optional<Comments> findByUuid(UUID uuid);
}
