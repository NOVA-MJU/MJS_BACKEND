package nova.mjs.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "department_schedules")
public class DepartmentSchedule extends BaseEntity {

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

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column //length 제한할 것인지?
    private String content;
}
