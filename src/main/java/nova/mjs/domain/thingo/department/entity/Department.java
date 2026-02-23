package nova.mjs.domain.thingo.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.admin.account.DTO.AdminDTO;
import nova.mjs.domain.thingo.department.dto.DepartmentDTO;
import nova.mjs.domain.thingo.department.entity.mapping.DepartmentAdmin;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.util.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "department",uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_department_college_name",
                columnNames = {"college", "department_name"})})
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private College college;

    @Enumerated(EnumType.STRING)
    @Column
    private DepartmentName departmentName;

    // 교학팀 전화번호
    @Column
    private String academicOfficePhone;

    @Column
    private String instagramUrl;

    @Column
    private String homepageUrl;

    @Builder.Default
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepartmentAdmin> departmentAdmins = new ArrayList<>();

    // 관리자 없이 생성
    public static Department create(DepartmentDTO.CreateRequest request) {
        return Department.builder()
                .college(request.getCollege())
                .departmentName(request.getDepartmentName())
                .academicOfficePhone(request.getAcademicOfficePhone())
                .homepageUrl(request.getHomepageUrl())
                .instagramUrl(request.getInstagramUrl())
                .build();
    }

    public static Department createWithAdmin(
            DepartmentDTO.CreateRequest request,
            Member admin
    ) {
        Department department = Department.builder()
                .college(request.getCollege())
                .departmentName(request.getDepartmentName())
                .academicOfficePhone(request.getAcademicOfficePhone())
                .homepageUrl(request.getHomepageUrl())
                .instagramUrl(request.getInstagramUrl())
                .build();

        department.assignAdmin(admin);
        return department;
    }

    public void updateInfo(AdminDTO.StudentCouncilUpdateDTO request) {
        this.departmentName = getOrDefault(request.getDepartmentName(), this.departmentName);
        this.homepageUrl = getOrDefault(request.getHomepageUrl(), this.homepageUrl);
        this.instagramUrl = getOrDefault(request.getInstagramUrl(), this.instagramUrl);
        this.college = getOrDefault(request.getCollege(), this.college);
    }

    /* ==========================================================
     * 상태 변경 (Admin 전용 업데이트)
     *
     * - null 값은 기존 값 유지
     * - DTO 의존은 허용 (현재 설계 선택 기준)
     * ========================================================== */
    public void updateAdminInfo(DepartmentDTO.UpdateRequest request) {

        this.college = getOrDefault(request.getCollege(), this.college);
        this.departmentName = getOrDefault(request.getDepartmentName(), this.departmentName);
        this.academicOfficePhone = getOrDefault(request.getAcademicOfficePhone(), this.academicOfficePhone);
        this.instagramUrl = getOrDefault(request.getInstagramUrl(), this.instagramUrl);
        this.homepageUrl = getOrDefault(request.getHomepageUrl(), this.homepageUrl);
    }



    // 관리자 변경
    public void assignAdmin(Member newAdmin) {
        if (!hasAdminEmail(newAdmin.getEmail())) {
            this.departmentAdmins.add(DepartmentAdmin.create(this, newAdmin));
        }
    }

    public boolean hasAdminEmail(String email) {
        return this.departmentAdmins.stream()
                .anyMatch(departmentAdmin -> departmentAdmin.getAdmin().getEmail().equals(email));
    }

    private <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }
}
