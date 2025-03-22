package nova.mjs.comments.entity;
import lombok.*;
import jakarta.persistence.*;
import nova.mjs.comments.DTO.CommentsResponseDto;
import nova.mjs.comments.likes.entity.LikeComment;
import nova.mjs.util.entity.BaseEntity;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;
import org.w3c.dom.Text;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class Comments extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comments_id")
    private long id; // 댓글 id

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_board_id", nullable = false)
    private CommunityBoard communityBoard ; // 댓글이 속한 게시물

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 작성자의 nickname, id

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 내용

    @Column
    private int likeCount; // 좋아요 수

    @OneToMany(mappedBy = "comments", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikeComment> likeComments = new ArrayList<>();



    public static Comments create(CommunityBoard communityBoard, Member member, String content) {
        return Comments.builder()
                .uuid(UUID.randomUUID())
                .communityBoard(communityBoard)
                .member(member)
                .content(content)
                .likeCount(0) // 기본값 설정
                .build();
    }

    // 게시물 좋아요
    public void increaseLikeCommentCount() {
        this.likeCount++;
    }

    public void decreaseLikeCommentCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
