package nova.mjs.domain.thingo.community.comment.likes.repository;

import nova.mjs.domain.thingo.community.comment.entity.Comment;
import nova.mjs.domain.thingo.community.comment.likes.entity.CommentLike;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 회원과 댓글을 기준으로 좋아요 여부 확인
    Optional<CommentLike> findByMemberAndComment(Member member, Comment comment);


    List<CommentLike> findByMemberAndComment_UuidIn(Member member, List<UUID> commentUuids);
}
