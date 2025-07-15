package nova.mjs.domain.department.repository;

import nova.mjs.domain.department.entity.DepartmentNotice;
import nova.mjs.domain.department.entity.DepartmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentNoticeRepository extends JpaRepository<DepartmentNotice, Long> {

}
