package nova.mjs.community.likes.repository;

import nova.mjs.community.likes.entity.LikeCommunity;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<LikeCommunity, Long> {

    // 회원과 게시글을 기준으로 좋아요 여부 확인
    Optional<LikeCommunity> findByMemberAndCommunityBoard(Member member, CommunityBoard communityBoard);

    // 특정 게시글의 좋아요 개수 조회
    @Query("SELECT COUNT(l) FROM LikeCommunity l WHERE l.communityBoard.uuid = :boardUUID")
    int countByCommunityBoardUuid(@Param("boardUUID") UUID boardUUID);
}
