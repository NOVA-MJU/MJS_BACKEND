package nova.mjs.domain.community.entity;


import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.community.comment.entity.Comment;
import nova.mjs.domain.community.entity.enumList.CommunityCategory;
import nova.mjs.domain.community.likes.entity.CommunityLike;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.util.ElasticSearch.EntityListner.CommunityEntityListener;
import nova.mjs.util.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static nova.mjs.domain.community.util.ContentPreviewUtil.makePreview;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(CommunityEntityListener.class)
@Table(name = "community_board")
public class CommunityBoard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_board_id")
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityCategory category; // 게시판 카테고리: 추후 확장성을 위하여 고려

    @Column(nullable = false)
    private String title; // 게시판 제목

    @Column(columnDefinition = "TEXT")
    private String content; // 내용 + 이미지 url 함께 구성되어있음

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author; // 작성자

    @Column(columnDefinition = "TEXT")
    private String previewContent; // 게시글 미리보기

    @Column(nullable = false)
    private int viewCount; // 조회 수

    @Column
    private int likeCount; // 게시글 좋아요 여부

    @Column
    private Boolean published; // 임시저장 여부

    @Column
    private LocalDateTime publishedAt;  // 공개 게시 시간

    @Builder.Default
    @OneToMany(mappedBy = "communityBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityLike> communityLike = new ArrayList<>();

    // 댓글
    @OneToMany(mappedBy = "communityBoard", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Comment> comment;


    // === 생성 메서드 ===
    public static CommunityBoard create(String title, String content, String previewContent, CommunityCategory category, Boolean published, Member member) {
        CommunityBoard board = CommunityBoard.builder()
                .uuid(UUID.randomUUID())
                .title(title)
                .content(content)
                .category(category)
                .published(published != null ? published : false)
                .previewContent(previewContent)
                .viewCount(0)
                .likeCount(0)
                .publishedAt(published != null && published ? LocalDateTime.now() : null)
                .author(member)
                .build();
        return board;
    }

    // === 업데이트 메서드 ===
    public void update(String title, String content, String contentPreview, Boolean published) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (contentPreview != null) this.previewContent = contentPreview;
        if (published != null) {
            updatePublishedState(published);
        }
    }

    // 발행 상태 업데이트 메서드
    private void updatePublishedState(Boolean isPublished) {
        if (isPublished && !this.published) {
            this.publishedAt = LocalDateTime.now();
        } else if (!isPublished && this.published) {
            this.publishedAt = null;
        }
        this.published = isPublished;
    }

    // 게시물 좋아요
    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    private <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }
}
