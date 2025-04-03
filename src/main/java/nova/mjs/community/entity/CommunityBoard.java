package nova.mjs.community.entity;


import jakarta.persistence.*;
import lombok.*;
import nova.mjs.comment.entity.Comment;
import nova.mjs.community.entity.enumList.CommunityCategory;
import nova.mjs.community.likes.entity.CommunityLike;
import nova.mjs.member.Member;
import nova.mjs.util.entity.BaseEntity;

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
    private CommunityCategory category; // 게시판 카테고리: 추후 확장성을 위하여 고려

    @Column(nullable = false)
    private String title; // 게시판 제목

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content; // 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author; // 작성자

    @Column(columnDefinition = "TEXT")
    private String previewContent; // 댓글 미리보기


    @ElementCollection
    @CollectionTable(
            name = "community_board_images", // 테이블 이름
            joinColumns = @JoinColumn(name = "community_board_id") // 외래 키
    )
    @Column(name = "content_image_url") // 컬럼 이름
    @Builder.Default
    private List<String> contentImages = new ArrayList<>(); // 기본 초기화

    @Column(nullable = false)
    private int viewCount; // 조회 수

    @Column
    private int likeCount; // 게시글 좋아요 여부

    @Column
    private Boolean published; // 임시저장 여부

    @Column
    private LocalDateTime publishedAt;  // 공개 게시 시간

    @OneToMany(mappedBy = "communityBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityLike> communityLike = new ArrayList<>();


    // === 생성 메서드 ===
    public static CommunityBoard create(String title, String content, CommunityCategory category, Boolean published, List<String> contentImages, Member member) {
        CommunityBoard board = CommunityBoard.builder()
                .uuid(UUID.randomUUID())
                .title(title)
                .content(content)
                .category(category)
                .published(published != null ? published : false)
                .previewContent(makePreview(content))
                .viewCount(0)
                .likeCount(0)
                .publishedAt(published != null && published ? LocalDateTime.now() : null)
                .author(member)
                .build();

        board.contentImages.addAll(contentImages != null ? contentImages : new ArrayList<>()); // null일 경우 빈 리스트 추가
        return board;
    }



    // === 업데이트 메서드 ===
    public void update(String title, String content, Boolean published, List<String> contentImages) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (published != null) {
            updatePublishedState(published);
        }
        if (contentImages != null) {
            updateContentImages(contentImages);
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

    // 이미지 리스트 업데이트 메서드
    private void updateContentImages(List<String> newContentImages) {
        this.contentImages.clear();
        this.contentImages.addAll(newContentImages);
    }

    // 댓글
    @OneToMany(mappedBy = "communityBoard", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Comment> comment;

    // 게시물 좋아요
    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    // 미리보기 생성 유틸
    private static String makePreview(String content) {
        return content.length() <= 60 ? content : content.substring(0, 60);
    }
}
