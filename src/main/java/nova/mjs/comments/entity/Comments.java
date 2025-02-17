package nova.mjs.comments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @ManyToOne
    @JoinColumn(name = "community_board_id", nullable = false)
    private CommunityBoard community_board ; // 댓글이 속한 게시물

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 작성자의 nickname, id

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 내용

    @Column
    private int likes; // 좋아요 수

    @Column(nullable = false)
    private LocalDateTime createDate; // 댓글 작성 날짜


    public static Comments create(CommunityBoard communityBoard, Member member, String content, int likes, LocalDateTime createDate) {
        return Comments.builder()
                .community_board(communityBoard)
                .member(member) // 왜 이렇게 해? Nickname이나 id 이렇게 가져오면 안되는건가
                .content(content)
                .likes(likes)
                .createDate(createDate)
                .build();
    }
}
