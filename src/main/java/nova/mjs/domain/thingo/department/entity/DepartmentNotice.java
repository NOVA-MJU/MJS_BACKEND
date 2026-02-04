package nova.mjs.domain.thingo.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.thingo.ElasticSearch.EntityListner.DepartmentNoticeEntityListener;
import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeRequestDTO;
import nova.mjs.util.entity.BaseEntity;
import nova.mjs.util.s3.S3DomainType;

import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(DepartmentNoticeEntityListener.class)
@Table(name = "department_notice")
public class DepartmentNotice extends BaseEntity {

    // 1) PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 2) department_notice_uuid 
    @Column(nullable = false, unique = true)
    private UUID uuid;

    // 3) FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // 4) title
    @Column(name = "title", nullable = false)
    private String title;

    // 5) content
    @Column(columnDefinition = "TEXT")
    private String content;

    // 6) preview_content
    @Column(name = "preview_content", columnDefinition = "TEXT")
    private String previewContent;

    // 7) thumbnail_url
    @Column
    private String thumbnailUrl;


    /* =================== 생성 =================== */
    public static DepartmentNotice create(AdminDepartmentNoticeRequestDTO request, Department department) {
        return DepartmentNotice.builder()
                .uuid(UUID.randomUUID())
                .title(request.getTitle())
                .content(request.getContent())
                .previewContent(request.getContentPreview())
                .thumbnailUrl(hasText(request.getThumbnailUrl())
                                ? request.getThumbnailUrl()
                                : S3DomainType.DEFAULT_THUMBNAIL_URL.getPrefix())
                .department(department)
                .build();
    }

    /* =================== 수정 =================== */
    public void update(AdminDepartmentNoticeRequestDTO requestDTO) {
        this.title = getOrDefault(requestDTO.getTitle(), this.title);
        this.content = getOrDefault(requestDTO.getContent(), this.content);
        this.previewContent = getOrDefault(requestDTO.getContentPreview(), this.previewContent); // content 변경 기준
        this.thumbnailUrl = getOrDefault(requestDTO.getThumbnailUrl(), this.thumbnailUrl);
    }

    private <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }

}