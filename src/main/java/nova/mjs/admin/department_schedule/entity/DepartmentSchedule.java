package nova.mjs.admin.department_schedule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.admin.account.entity.Admin;
import nova.mjs.util.entity.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "department-schedule")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DepartmentSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false)
    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(columnDefinition = "TEXT") // 검증 필요
    private String content;

    @Column(nullable = false)
    private String colorCode; // #FF5733

    @ManyToOne(fetch = FetchType.LAZY)
    private Admin admin;
}