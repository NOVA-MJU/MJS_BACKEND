package nova.mjs.domain.thingo.department.repository;

import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 학과 일정 Repository (V2)
 *
 * 학과 식별은 Department 엔티티 기준
 */
public interface DepartmentScheduleRepository
        extends JpaRepository<DepartmentSchedule, Long> {

    /**
     * 특정 학과의 일정 전체 조회
     */
    List<DepartmentSchedule> findByDepartment(Department department);

    /**
     * 일정 단건 조회 (고유 UUID)
     */
    Optional<DepartmentSchedule> findByDepartmentScheduleUuid(UUID scheduleUuid);

    /**
     * 학과 + 일정 UUID 기준 조회 (권한 검증용)
     */
    Optional<DepartmentSchedule> findByDepartmentAndDepartmentScheduleUuid(
            Department department,
            UUID scheduleUuid
    );
}
