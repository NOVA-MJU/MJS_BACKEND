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
    @Column(name = "comments")
    private long id; // 댓글 id

    @ManyToOne
    @JoinColumn(name = "uuid", nullable = false)
    private Member uuid; // 작성자 아이디

    @ManyToOne
    @JoinColumn(name = "nickname", nullable = false)
    private Member nickname; // 작성자 닉네임

    @Column(nullable = false)
    private String content; // 내용

    @Column
    private int likes; // 좋아요 수

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityBoard community_board_id ; // 댓글이 속한 게시물


    public static Comments create(Member nickname, String content, int likes) {
        return Comments.builder()
                .nickname(nickname)
                .content(content)
                .likes(likes)
                .build();
    }
}
