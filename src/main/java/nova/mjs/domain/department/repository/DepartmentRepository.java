package nova.mjs.domain.department.repository;

import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.member.entity.enumList.College;
import nova.mjs.domain.member.entity.enumList.DepartmentName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentName(DepartmentName departmentName);

    Optional<Department> findByDepartmentUuid(UUID uuid);

    List<Department> findByCollege(College college);

    @Query("SELECT d.admin.email FROM Department d WHERE d.departmentUuid = :departmentUuid")
    Optional<String> findAdminEmailByDepartmentUuid(@Param("departmentUuid") UUID departmentUuid);

    @Query("SELECT d FROM Department d WHERE d.admin.email = :email")
    Optional<Department> findByAdminEmail(@Param("email") String email);

}
