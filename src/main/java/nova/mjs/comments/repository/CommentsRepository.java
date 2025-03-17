package nova.mjs.comments.repository;

import nova.mjs.comments.entity.Comments;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT COUNT(c) FROM Comments c WHERE c.communityBoard.uuid = :boardUUID")
    int countByCommunityBoardUuid(@Param("boardUUID") UUID boardUUID);

    // 특정 회원이 댓글을 작성한 게시물 리스트 조회 (중복 방지)
    @Query("SELECT DISTINCT c.communityBoard FROM Comments c WHERE c.member = :member")
    List<CommunityBoard> findDistinctCommunityBoardByMember(@Param("member") Member member);

}
