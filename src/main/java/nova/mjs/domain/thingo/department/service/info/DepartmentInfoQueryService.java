package nova.mjs.domain.thingo.department.service.info;


import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.dto.DepartmentDTO;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.exception.CollegeNotFoundException;
import nova.mjs.domain.thingo.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentInfoQueryService {

    private final DepartmentRepository departmentRepository;

    /**
     * 학과 정보 단건 조회
     * - college + department 기준
     */
    public DepartmentDTO.InfoResponse getDepartmentInfo(
            College college,
            DepartmentName departmentName
    ) {
        Department department;

        if (departmentName == null) {
            department = departmentRepository
                    .findCollegeLevelDepartment(college)
                    .orElseThrow(CollegeNotFoundException::new);
        } else {
            department = departmentRepository
                    .findByCollegeAndDepartmentName(college, departmentName)
                    .orElseThrow(DepartmentNotFoundException::new);
        }

        return DepartmentDTO.InfoResponse.fromEntity(department);
    }
}
