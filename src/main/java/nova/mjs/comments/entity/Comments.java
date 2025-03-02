package nova.mjs.comments.entity;
import lombok.*;
import jakarta.persistence.*;
import nova.mjs.comments.DTO.CommentsResponseDto;
import nova.mjs.util.entity.BaseEntity;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;
import org.w3c.dom.Text;

import java.time.LocalDateTime;
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
    private int likes; // 좋아요 수


    public static Comments create(CommunityBoard communityBoard, Member member, String content) {
        return Comments.builder()
                .uuid(UUID.randomUUID())
                .communityBoard(communityBoard)
                .member(member)
                .content(content)
                .likes(0) // 기본값 설정
                .build();
    }
}
