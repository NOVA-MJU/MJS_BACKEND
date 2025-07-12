package nova.mjs.domain.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.member.entity.enumList.College;
import nova.mjs.domain.member.entity.enumList.DepartmentName;
import nova.mjs.util.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "department")
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID departmentUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepartmentName departmentName;

    @Column
    private String studentCouncilName; // 학생회 명

    @Column
    private String studentCouncilLogo;

    @Column
    private String instagramUrl;

    @Column
    private String homepageUrl;

    @Column
    private String slogan;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private College college;

    @Builder.Default
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepartmentSchedule> schedules = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepartmentNotice> notices = new ArrayList<>();

    public void updateInfo(DepartmentName departmentName, String studentCouncilName, String homepageUrl, String instagramUrl, String description, String studentCouncilLogo, String slogan, College college) {
        this.departmentName = departmentName;
        this.studentCouncilName = studentCouncilName;
        this.homepageUrl = homepageUrl;
        this.instagramUrl = instagramUrl;
        this.description = description;
        this.studentCouncilLogo = studentCouncilLogo;
        this.slogan = slogan;
        this.college = college;
    }
}