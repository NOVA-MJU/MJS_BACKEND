package nova.mjs.domain.thingo.department.service.info;


import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.dto.DepartmentDTO;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
        Department department = departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(DepartmentNotFoundException::new);

        return DepartmentDTO.InfoResponse.fromEntity(department);
    }
}
