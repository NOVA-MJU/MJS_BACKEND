package nova.mjs.domain.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.admin.department.department_schedule.dto.AdminDepartmentScheduleRequestDTO;
import nova.mjs.util.ElasticSearch.EntityListner.DepartmentScheduleEntityListener;
import nova.mjs.util.entity.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(DepartmentScheduleEntityListener.class)
@Table(name = "department_schedules")
public class DepartmentSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID departmentScheduleUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = false)
    private String colorCode;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column
    private String content;

    public void updateFromRequest(AdminDepartmentScheduleRequestDTO request){
        if (request.getTitle() != null) this.title = request.getTitle();
        if (request.getContent() != null) this.content = request.getContent();
        if (request.getColorCode() != null) this.colorCode = request.getColorCode();
        if (request.getStartDate() != null) this.startDate = request.getStartDate();
        if (request.getEndDate() != null) this.endDate = request.getEndDate();
    }
}
