package nova.mjs.domain.thingo.community.likes.repository;

import nova.mjs.domain.thingo.community.likes.entity.CommunityLike;
import nova.mjs.domain.thingo.community.entity.CommunityBoard;
import nova.mjs.domain.thingo.community.repository.projection.UuidCount;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {

    // 회원과 게시글을 기준으로 좋아요 여부 확인
    Optional<CommunityLike> findByMemberAndCommunityBoard(Member member, CommunityBoard communityBoard);

    // 특정 게시글의 좋아요 개수 조회
    @Query("SELECT COUNT(likeCommunity) FROM CommunityLike likeCommunity WHERE likeCommunity.communityBoard.uuid = :boardUUID")
    int countByCommunityBoardUuid(@Param("boardUUID") UUID boardUUID);

    // 특정 회원이 찜한 게시물 리스트 조회
    @Query("SELECT likeCommunity.communityBoard FROM CommunityLike likeCommunity WHERE likeCommunity.member = :member")
    Page<CommunityBoard> findCommunityBoardsByMember(@Param("member") Member member, Pageable pageable);

    @Query(" SELECT cl.communityBoard.uuid FROM CommunityLike cl WHERE cl.member = :member AND cl.communityBoard.uuid IN :communityBoardUUID")
    List<UUID> findCommunityUuidsLikedByMember(
            @Param("member") Member member,
            @Param("communityBoardUUID") List<UUID> communityBoardUUID
    );

    int countByMember(Member member);

    // CommunityLikeRepository
    @Query("""
    select cb.uuid as uuid, count(l) as cnt
    from CommunityLike l
    join l.communityBoard cb
    where cb.uuid in :uuids
    group by cb.uuid
""")
    List<UuidCount> countLikesByBoardUuids(@Param("uuids") List<UUID> uuids);


}
