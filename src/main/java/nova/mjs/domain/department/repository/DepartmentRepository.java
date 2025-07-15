package nova.mjs.domain.department.repository;

import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.member.entity.enumList.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentName(String departmentName);

    Optional<Department> findByDepartmentUuid(UUID uuid);

    List<Department> findByCollege(College college);

}
