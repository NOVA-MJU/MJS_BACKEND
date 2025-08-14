package nova.mjs.domain.community.repository;

import nova.mjs.domain.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.community.entity.CommunityBoard;
import nova.mjs.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBoardRepository extends JpaRepository<CommunityBoard, Long> {
    Optional<CommunityBoard> findByUuid(UUID uuid);

    // 댓글 lazy loading 해결을 위한 fetch join
    // 댓글을 한 번에 가져옴
    // 쿼리 최적화 (N + 1 문제 방지)로 성능을 향상시킬 수 있음
    @Query("SELECT cb from CommunityBoard cb JOIN fetch cb.comment where cb.uuid = :uuid")
    Optional<CommunityBoard> findByUuidWithComment(@Param("uuid") UUID uuid);

    // 내가 작성한 게시글 조회
    List<CommunityBoard> findByAuthor(Member author);


    int countByAuthor(Member author);

    @EntityGraph(attributePaths = "author")
    Page<CommunityBoard> findAll(Pageable pageable);

    @Query("""
    SELECT cb
    FROM CommunityBoard cb
    JOIN FETCH cb.author
    """)
    Page<CommunityBoard> findAllWithAuthor(Pageable pageable);

    @Query("SELECT b FROM CommunityBoard b " +
            "WHERE b.publishedAt >= :after AND b.published = true " +
            "ORDER BY b.likeCount DESC")
    List<CommunityBoard> findTop3PopularBoards(@Param("after") LocalDateTime after, Pageable pageable);


    @EntityGraph(attributePaths = "author")
    @Query("""
select cb
from CommunityBoard cb
where cb.uuid not in :excluded
""")
    Page<CommunityBoard> findAllWithAuthorExcluding(@Param("excluded") List<UUID> excluded, Pageable pageable);

}
