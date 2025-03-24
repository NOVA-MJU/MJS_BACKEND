package nova.mjs.comment.repository;

import nova.mjs.comment.entity.Comment;
import nova.mjs.community.entity.CommunityBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentsRepository extends JpaRepository<Comment, Long> {

    // ✅ 페이징을 적용하여 특정 게시글의 댓글을 조회
    Page<Comment> findByCommunityBoard(CommunityBoard communityBoard, Pageable pageable);
    Optional<Comment> findByUuid(UUID uuid);

    // 특정 댓글의 좋아요 개수 조회
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.communityBoard.uuid = :boardUUID")
    int countByCommunityBoardUuid(@Param("boardUUID") UUID boardUUID);
}
