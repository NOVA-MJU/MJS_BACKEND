package nova.mjs.comments.likes.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.comments.entity.Comments;
import nova.mjs.member.Member;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "like_comment")
public class LikeComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comments_id", nullable = false)
    private Comments comments;

    public LikeComment(Member member, Comments comments) {
        this.member = member;
        this.comments = comments;
    }
}
