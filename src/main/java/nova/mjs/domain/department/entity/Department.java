package nova.mjs.domain.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.admin.account.DTO.AdminDTO;
import nova.mjs.domain.member.entity.Member;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_member_id") // nullable 허용
    private Member admin; // 관리자로 연결된 회원

    @Column(nullable = false, unique = true)
    private UUID departmentUuid;

    @Enumerated(EnumType.STRING)
    private DepartmentName departmentName;

    @Column
    private String slogan;

    @Column
    private String description;

    @Column
    private String studentCouncilContactEmail; // 학생회 명

    @Column
    private String instagramUrl;

    @Column
    private String homepageUrl;

    @Enumerated(EnumType.STRING)
    private College college;

    @Builder.Default
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepartmentSchedule> schedules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepartmentNotice> notices = new ArrayList<>();


    public static Department createWithAdmin(AdminDTO.StudentCouncilInitRegistrationRequestDTO dto, Member admin) {
        return Department.builder()
            .departmentUuid(UUID.randomUUID())
            .studentCouncilContactEmail(dto.getContactEmail())
            .admin(admin)
            .build();
    }

    public void updateInfo(AdminDTO.StudentCouncilUpdateDTO request) {
        this.departmentName = getOrDefault(request.getDepartmentName(), this.departmentName);
        this.homepageUrl = getOrDefault(request.getHomepageUrl(), this.homepageUrl);
        this.instagramUrl = getOrDefault(request.getInstagramUrl(), this.instagramUrl);
        this.description = getOrDefault(request.getDescription(), this.description);
        this.slogan = getOrDefault(request.getSlogan(), this.slogan);
        this.college = getOrDefault(request.getCollege(), this.college);
    }

    // 어드민 변경
    public void assignAdmin(Member newAdmin) {
        this.admin = newAdmin;
    }

    private <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }
}