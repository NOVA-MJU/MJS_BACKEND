package nova.mjs.domain.community.repository;

import nova.mjs.domain.community.entity.CommunityBoard;
import nova.mjs.domain.community.entity.enumList.CommunityCategory;
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

/**
 * CommunityBoardRepository
 *
 * 역할
 * - 게시글 단건 조회, 페이징 조회
 * - 인기글 조회 (likeCount DESC 상위 N개)
 * - 카테고리별 조회/제외 조회
 *
 * 주의
 * - @EntityGraph(attributePaths = "author") 로 author를 항상 함께 로딩해서
 *   Service 단에서 N+1 문제 없이 작성자 닉네임 등을 바로 DTO로 변환할 수 있도록 한다.
 * - 인기글 UUID를 제외하고 일반글을 조회할 때 NOT IN (:excluded)를 쓰므로,
 *   excluded 리스트가 비어있을 경우는 Service에서 분기 처리한다.
 */
@Repository
public interface CommunityBoardRepository extends JpaRepository<CommunityBoard, Long> {

    /**
     * UUID로 게시글 단건 조회
     */
    Optional<CommunityBoard> findByUuid(UUID uuid);

    /**
     * 댓글 lazy loading 해결용: 게시글 + 댓글까지 fetch join
     * - 주의: 컬렉션 fetch join + 페이징은 위험하므로 상세 조회 전용에서만 사용
     */
    @Query("""
        SELECT cb
        FROM CommunityBoard cb
        JOIN FETCH cb.comment
        WHERE cb.uuid = :uuid
    """)
    Optional<CommunityBoard> findByUuidWithComment(@Param("uuid") UUID uuid);

    /**
     * 특정 작성자가 쓴 게시글 전체 조회 (마이페이지 등)
     */
    Page<CommunityBoard> findByAuthor(Member author, Pageable pageable);

    /**
     * 특정 작성자가 쓴 게시글 개수
     */
    int countByAuthor(Member author);

    /**
     * ===== 인기글 조회 =====
     *
     * 조건:
     * - 최근 after 이후에 작성(publishedAt >= after)
     * - 공개 게시글만(published = true)
     * - 좋아요 많은 순서대로
     * - Pageable 로 top N (보통 PageRequest.of(0,3))
     */

    // 전체 카테고리에서 인기글 TOP N
    @Query("""
        SELECT board
        FROM CommunityBoard board
        WHERE board.publishedAt >= :after
          AND board.published = true
        ORDER BY board.likeCount DESC
    """)
    List<CommunityBoard> findTop3PopularBoards(
            @Param("after") LocalDateTime after,
            Pageable pageable
    );

    // 특정 카테고리 안에서만 인기글 TOP N
    @Query("""
        SELECT b
        FROM CommunityBoard b
        WHERE b.publishedAt >= :after
          AND b.published = true
          AND b.category = :category
        ORDER BY b.likeCount DESC
    """)
    List<CommunityBoard> findTop3PopularBoardsByCategory(
            @Param("after") LocalDateTime after,
            @Param("category") CommunityCategory category,
            Pageable pageable
    );

    /**
     * ===== 일반글 / 전체글 조회 =====
     *
     * author를 즉시 로딩해서 N+1 방지
     *
     * 기본 규칙:
     * - findAllWithAuthor(...)                     : 전체 카테고리 조회
     * - findAllWithAuthorExcluding(..., excluded)  : 전체 카테고리 + 인기글 UUID 제외
     * - findAllWithAuthorByCategory(category, ...) : 특정 카테고리만
     * - findAllWithAuthorByCategoryExcluding(...)  : 특정 카테고리만 + 인기글 UUID 제외
     */

    // 전체 카테고리
    @EntityGraph(attributePaths = "author")
    @Query("""
        SELECT b
        FROM CommunityBoard b
    """)
    Page<CommunityBoard> findAllWithAuthor(Pageable pageable);

    // 전체 카테고리 - 인기글 제외
    @EntityGraph(attributePaths = "author")
    @Query("""
        SELECT b
        FROM CommunityBoard b
        WHERE b.uuid NOT IN :excluded
    """)
    Page<CommunityBoard> findAllWithAuthorExcluding(
            @Param("excluded") List<UUID> excluded,
            Pageable pageable
    );

    // 특정 카테고리
    @EntityGraph(attributePaths = "author")
    @Query("""
        SELECT b
        FROM CommunityBoard b
        WHERE b.category = :category
    """)
    Page<CommunityBoard> findAllWithAuthorByCategory(
            @Param("category") CommunityCategory category,
            Pageable pageable
    );

    // 특정 카테고리 - 인기글 제외
    @EntityGraph(attributePaths = "author")
    @Query("""
        SELECT b
        FROM CommunityBoard b
        WHERE b.category = :category
          AND b.uuid NOT IN :excluded
    """)
    Page<CommunityBoard> findAllWithAuthorByCategoryExcluding(
            @Param("category") CommunityCategory category,
            @Param("excluded") List<UUID> excluded,
            Pageable pageable
    );
}
