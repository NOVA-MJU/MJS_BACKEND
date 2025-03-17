package nova.mjs.comments.likes.repository;

import nova.mjs.comments.entity.Comments;
import nova.mjs.comments.likes.entity.LikeComment;
import nova.mjs.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeCommentRepository extends JpaRepository<LikeComment, Long> {

    // 회원과 댓글을 기준으로 좋아요 여부 확인
    Optional<LikeComment> findByMemberAndComments(Member member, Comments comments);

    // 특정 댓글의 좋아요 개수 조회
    @Query("SELECT COUNT(l) FROM LikeComment l WHERE l.comments.uuid = :commentsUUID")
    int countByCommentsUuid(@Param("commentsUUID") UUID commentsUUID);

}
