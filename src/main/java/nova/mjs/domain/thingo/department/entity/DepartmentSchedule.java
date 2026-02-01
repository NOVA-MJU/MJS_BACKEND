package nova.mjs.domain.thingo.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleRequestDTO;
import nova.mjs.domain.mentorship.ElasticSearch.EntityListner.DepartmentScheduleEntityListener;
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

    @Column(columnDefinition = "TEXT")   // PG: 사실상 길이 제한 없음
    private String content;

    public static DepartmentSchedule create(AdminDepartmentScheduleRequestDTO dto, Department department) {
        return DepartmentSchedule.builder()
                .departmentScheduleUuid(UUID.randomUUID())
                .title(dto.getTitle())
                .content(dto.getContent())
                .colorCode(dto.getColorCode())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .department(department)
                .build();
    }

    public void update(AdminDepartmentScheduleRequestDTO request) {
        this.title = getOrDefault(request.getTitle(), this.title);
        this.content = getOrDefault(request.getContent(), this.content);
        this.colorCode = getOrDefault(request.getColorCode(), this.colorCode);
        this.startDate = getOrDefault(request.getStartDate(), this.startDate);
        this.endDate = getOrDefault(request.getEndDate(), this.endDate);
    }

    // 내부 유틸 메서드
    private <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }

}
