package nova.mjs.domain.thingo.department.entity.mapping;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.util.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "department_admin",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_department_admin_department_member",
                        columnNames = {"department_id", "admin_member_id"}
                )
        }
)
public class DepartmentAdmin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_admin_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_member_id", nullable = false)
    private Member admin;

    public static DepartmentAdmin create(Department department, Member admin) {
        return DepartmentAdmin.builder()
                .department(department)
                .admin(admin)
                .build();
    }
}
