package nova.mjs.domain.thingo.department.repository;

import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.mapping.DepartmentAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentAdminRepository extends JpaRepository<DepartmentAdmin, Long> {

    boolean existsByDepartmentAndAdminEmail(Department department, String email);

    Optional<DepartmentAdmin> findFirstByAdminEmail(String email);
}
