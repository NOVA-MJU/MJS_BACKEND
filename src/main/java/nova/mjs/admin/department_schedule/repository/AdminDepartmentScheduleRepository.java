package nova.mjs.admin.department_schedule.repository;

import nova.mjs.department.entity.Department;
import nova.mjs.department.entity.DepartmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdminDepartmentScheduleRepository extends JpaRepository<DepartmentSchedule, Long> {
    List<DepartmentSchedule> findAllByDepartmentAndStartDateBetween(Department department, LocalDate start, LocalDate end);
}