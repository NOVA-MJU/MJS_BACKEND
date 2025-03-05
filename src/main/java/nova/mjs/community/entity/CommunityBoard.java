package nova.mjs.community.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.comments.entity.Comments;
import nova.mjs.community.entity.enumList.CommunityCategory;
import nova.mjs.util.entity.BaseEntity;
import nova.mjs.member.entity.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "community_board")
public class CommunityBoard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_board_id")
    private long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityCategory category;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member author;

    @ElementCollection
    @CollectionTable(name = "community_board_images", joinColumns = @JoinColumn(name = "community_board_id"))
    @Column(name = "content_image_url")
    @Builder.Default
    private List<String> contentImages = new ArrayList<>();

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private int likeCount;

    @Column
    private Boolean published;

    @Column
    private LocalDateTime publishedAt;

    // === 생성 메서드 ===
    public static CommunityBoard create(String title, String content, Member author, CommunityCategory category, Boolean published, List<String> contentImages) {
        CommunityBoard board = CommunityBoard.builder()
                .uuid(UUID.randomUUID())
                .title(title)
                .content(content)
                .author(author)
                .category(category)
                .published(published != null ? published : false)
                .viewCount(0)
                .likeCount(0)
                .publishedAt(published != null && published ? LocalDateTime.now() : null)
                .build();

        board.contentImages.addAll(contentImages != null ? contentImages : new ArrayList<>());
        return board;
    }

    // === 업데이트 메서드 ===
    public void update(String title, String content, Boolean published, List<String> contentImages) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (published != null) updatePublishedState(published);
        if (contentImages != null) updateContentImages(contentImages);
    }

    private void updatePublishedState(Boolean isPublished) {
        this.published = isPublished;
        this.publishedAt = isPublished ? LocalDateTime.now() : null;
    }

    private void updateContentImages(List<String> newContentImages) {
        this.contentImages.clear();
        this.contentImages.addAll(newContentImages);
    }

    public void increaseLike() {
        this.likeCount++;
    }

    public void decreaseLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    @OneToMany(mappedBy = "communityBoard", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Comments> comments;
}