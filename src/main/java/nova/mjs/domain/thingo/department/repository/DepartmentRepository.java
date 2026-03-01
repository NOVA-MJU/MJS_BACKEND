package nova.mjs.domain.thingo.department.repository;

import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByDepartmentName(DepartmentName departmentName);

    /**
     * 단과대 + 학과 기준 단건 조회
     */
    Optional<Department> findByCollegeAndDepartmentName(
            College college,
            DepartmentName departmentName
    );

    /**
     * 학과 존재 여부 확인
     */
    boolean existsByCollegeAndDepartmentName(
            College college,
            DepartmentName departmentName
    );

    /**
     * 단과대 기준 단건 조회
     * (departmentName == null)
     */
    Optional<Department> findByCollegeAndDepartmentNameIsNull(College college);

    /**
     * 단과대(학부) 레벨 Department 조회
     */
    default Optional<Department> findCollegeLevelDepartment(College college) {
        return findByCollegeAndDepartmentNameIsNull(college);
    }

    /**
     * ✅ 학과 레벨 Department 전체 조회
     * (departmentName != null)
     */
    List<Department> findAllByDepartmentNameIsNotNull();

    /**
     * ⚠️ 주의: 이 메서드는 단과대 레벨 + 학과 레벨을 모두 포함할 수 있습니다.
     * 정책상 "college만" 처리에서 사용하면 안 됩니다.
     */
    List<Department> findByCollege(College college);
}