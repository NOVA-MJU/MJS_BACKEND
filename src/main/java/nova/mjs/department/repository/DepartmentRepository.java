package nova.mjs.department.repository;

import nova.mjs.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);

    Optional<Department> findByDepartmentUuid(UUID uuid);

}
