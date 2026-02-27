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
     * 단과대 + 학과 기준 단건 조회 (V2 핵심)
     */
    Optional<Department> findByCollegeAndDepartmentName(
            College college,
            DepartmentName departmentName
    );

    /**
     * 학과 존재 여부 확인
     *
     * Service 계층에서 선 검증용으로 사용
     */
    boolean existsByCollegeAndDepartmentName(
            College college,
            DepartmentName departmentName
    );



    /**
     * 단과대기준 단건 조회 (V2 핵심)
     */
    Optional<Department> findByCollegeAndDepartmentNameIsNull(
            College college
    );

    /**
     * 단과대(학부) 레벨 Department 조회
     *
     * - departmentName == null 인 레코드를 의미한다.
     */
    default Optional<Department> findCollegeLevelDepartment(College college) {
        return findByCollegeAndDepartmentNameIsNull(college);
    }

    List<Department> findByCollege(College college);
}
