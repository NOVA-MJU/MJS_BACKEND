package nova.mjs.community.comment.repository;

import nova.mjs.community.comment.entity.Comment;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 페이징 해제
    List<Comment> findByCommunityBoard(CommunityBoard communityBoard);
    Optional<Comment> findByUuid(UUID uuid);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.communityBoard.uuid = :boardUUID")
    int countByCommunityBoardUuid(@Param("boardUUID") UUID boardUUID);

    // 특정 회원이 댓글을 작성한 게시물 리스트 조회 (중복 방지)
    @Query("SELECT DISTINCT c.communityBoard FROM Comment c WHERE c.member = :member")
    List<CommunityBoard> findDistinctCommunityBoardByMember(@Param("member") Member member);

    @Query("SELECT c FROM Comment c JOIN FETCH c.communityBoard WHERE c.member = :member")
    List<Comment> findByMember(@Param("member") Member member);

    int countByMember(Member member);


}
