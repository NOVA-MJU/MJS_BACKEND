package nova.mjs.domain.thingo.community.comment.likes.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.thingo.community.comment.entity.Comment;
import nova.mjs.domain.thingo.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor
@Builder
@Table(name = "like_comment")
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comments_id", nullable = false)
    private Comment comment;

    public static CommentLike create(Member member, Comment comment) {
        return CommentLike.builder()
                .member(member)
                .comment(comment)
                .build();

    }
//    추가 권장(중복 좋아요 방지)
//
//    DB에 유니크 제약이 없으면 동시 요청에서 중복 좋아요 row가 생길 수 있어.
//
//    CommentLike(member_id, comment_id) 유니크 인덱스 권장
//
//    CommunityLike(member_id, community_board_id)도 동일

}
