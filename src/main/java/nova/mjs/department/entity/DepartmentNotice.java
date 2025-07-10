package nova.mjs.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "department_notices")
public class DepartmentNotice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    private String title;

    @Column
    private String content;

    @ElementCollection
    @CollectionTable(
            name = "department_notice_images",
            joinColumns = @JoinColumn(name = "department_notice_id")
    )
    @Column(name = "content_image_urls", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> contentImages = new ArrayList<>();
}