package nova.mjs.domain.thingo.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.admin.department.notice.dto.AdminStudentCouncilNoticeDTO;
import nova.mjs.domain.thingo.ElasticSearch.EntityListner.StudentCouncilNoticeEntityListener;
import nova.mjs.util.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(StudentCouncilNoticeEntityListener.class)
@Table(name = "student_council_notices")
public class StudentCouncilNotice extends BaseEntity {

    /* PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* 비즈니스 식별자 */
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    /* 소속 학과 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /* 제목 (선택) */
    @Column
    private String title;

    /* 본문 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /* 작성자 snapshot */
    @Column(nullable = false)
    private String authorNickname;

    /* 게시 시각 (수정과 분리) */
    @Column(nullable = false)
    private LocalDateTime publishedAt;

    /* 이미지 목록 (최대 20장) */
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    @Builder.Default
    private List<StudentCouncilNoticeImage> images = new ArrayList<>();


    /* ==========================================================
     * 파생 데이터
     * ========================================================== */

    /** 리스트 조회 썸네일 = 첫 이미지 */
    public String getThumbnailUrl() {
        if (images == null || images.isEmpty()) return null;

        String url = images.get(0).getImageUrl();
        if (url == null || url.isBlank()) return null;

        return url;
    }



    /* ==========================================================
     * 생성
     * ========================================================== */

    public static StudentCouncilNotice create(
            AdminStudentCouncilNoticeDTO.Request request,
            Department department,
            String adminNickname
    ) {
        StudentCouncilNotice notice = StudentCouncilNotice.builder()
                .uuid(UUID.randomUUID())
                .department(department)
                .title(request.getTitle())
                .content(request.getContent())
                .authorNickname(adminNickname)
                .publishedAt(LocalDateTime.now())
                .build();

        notice.replaceImages(request.getImageUrls());
        return notice;
    }


    /* ==========================================================
     * 수정
     * ========================================================== */

    public void update(AdminStudentCouncilNoticeDTO.Request request) {
        this.title = getOrDefault(request.getTitle(), this.title);
        this.content = getOrDefault(request.getContent(), this.content);

        if (request.getImageUrls() != null) {
            replaceImages(request.getImageUrls());
        }
    }


    /* ==========================================================
     * 이미지 교체 (핵심 로직)
     * - clear 후 재삽입해야 순서/삭제 정상 동작
     * ========================================================== */
    /* ---------- 핵심 로직 ---------- */
    private void replaceImages(List<String> imageUrls) {

        this.images.clear();

        if (imageUrls == null || imageUrls.isEmpty()) return;

        int sequence = 0;

        for (String url : imageUrls) {

            if (url == null) continue;

            String trimmed = url.trim();
            if (trimmed.isEmpty()) continue;

            this.images.add(StudentCouncilNoticeImage.of(this, trimmed, sequence++));
        }

        if (this.images.size() > 20) {
            throw new IllegalArgumentException("이미지는 최대 20개까지 업로드 가능합니다.");
        }
    }

    private <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }
}
