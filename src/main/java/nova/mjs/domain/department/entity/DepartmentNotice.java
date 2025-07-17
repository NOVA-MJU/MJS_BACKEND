package nova.mjs.domain.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.ElasticSearch.EntityListner.DepartmentNoticeEntityListener;
import nova.mjs.util.entity.BaseEntity;

import java.util.UUID;

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
    private UUID departmentNoticeUuid;

    // 3) FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // 4) title
    @Column(name = "title", nullable = false)
    private String title;

    // 5) content
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    // 6) preview_content
    @Column(name = "preview_content", columnDefinition = "TEXT")
    private String previewContent;

    // 7) thumbnail_url
    @Column
    private String thumbnailUrl;

}