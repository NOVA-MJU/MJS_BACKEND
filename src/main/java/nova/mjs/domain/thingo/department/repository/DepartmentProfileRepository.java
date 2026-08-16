package nova.mjs.domain.thingo.department.repository;

import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentProfileRepository extends JpaRepository<DepartmentProfile, Long> {

    Optional<DepartmentProfile> findByDepartment(Department department);

    @EntityGraph(attributePaths = "department")
    List<DepartmentProfile> findAllByCollectionStatus(String collectionStatus);
}
