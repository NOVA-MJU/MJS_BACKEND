package nova.mjs.admin.department.department_schedule.repository;

import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.entity.DepartmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdminDepartmentScheduleRepository extends JpaRepository<DepartmentSchedule, Long> {
    List<DepartmentSchedule> findAllByDepartmentAndStartDateBetween(Department department, LocalDate start, LocalDate end);
}