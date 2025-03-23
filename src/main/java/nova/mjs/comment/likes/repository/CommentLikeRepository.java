package nova.mjs.comment.likes.repository;

import nova.mjs.comment.entity.Comment;
import nova.mjs.comment.likes.entity.CommentLike;
import nova.mjs.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 회원과 댓글을 기준으로 좋아요 여부 확인
    Optional<CommentLike> findByMemberAndComment(Member member, Comment comment);

    // 특정 댓글의 좋아요 개수 조회
    @Query("SELECT COUNT(l) FROM CommentLike l WHERE l.comment.uuid = :commentsUUID")
    int countByCommentUuid(@Param("commentsUUID") UUID commentsUUID);

}
