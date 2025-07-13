package nova.mjs.community.likes.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.member.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "like_community")
public class CommunityLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_like_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_board_id", nullable = false)
    private CommunityBoard communityBoard;

    public CommunityLike(Member member, CommunityBoard communityBoard) {
        this.member = member;
        this.communityBoard = communityBoard;
    }
}
