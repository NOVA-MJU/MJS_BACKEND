package nova.mjs.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.member.entity.enumList.College;
import nova.mjs.util.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "departments")
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID departmentUuid;

    @Column(nullable = false)
    private String departmentName;

    @Column
    private String studentCouncilName;

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

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepartmentSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepartmentNotice> notices = new ArrayList<>();

    public void updateInfo(String departmentName, String studentCouncilName, String homepageUrl, String instagramUrl, String description, String studentCouncilLogo, String slogan, College college) {
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