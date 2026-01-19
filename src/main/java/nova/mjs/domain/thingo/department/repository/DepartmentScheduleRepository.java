package nova.mjs.domain.thingo.department.repository;

import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentScheduleRepository extends JpaRepository<DepartmentSchedule, Long> {
    List<DepartmentSchedule> findByDepartment_DepartmentUuid(UUID departmentUuid);

    Optional<DepartmentSchedule> findByDepartmentScheduleUuid(UUID scheduleId);
}
