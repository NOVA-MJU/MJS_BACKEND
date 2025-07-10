package nova.mjs.admin.department_schedule.repository;

import nova.mjs.admin.account.entity.Admin;
import nova.mjs.admin.department_schedule.entity.DepartmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DepartmentScheduleRepository extends JpaRepository<DepartmentSchedule, Long> {
    List<DepartmentSchedule> findAllByAdminAndStartDateBetween(Admin admin, LocalDate startDate, LocalDate endDate);
}