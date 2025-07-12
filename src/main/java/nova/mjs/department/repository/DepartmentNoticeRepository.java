package nova.mjs.department.repository;

import nova.mjs.department.entity.DepartmentNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DepartmentNoticeRepository extends JpaRepository<DepartmentNotice, Long> {
    List<DepartmentNotice> findByDepartment_DepartmentUuid(UUID departmentUuid);
}