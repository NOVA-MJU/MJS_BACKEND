package nova.mjs.domain.department.service.info;


import lombok.RequiredArgsConstructor;
import nova.mjs.domain.department.dto.DepartmentInfoDTO;
import nova.mjs.domain.department.dto.DepartmentSummaryDTO;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.department.repository.DepartmentRepository;
import nova.mjs.domain.member.entity.enumList.College;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentInfoService {

    private final DepartmentRepository departmentRepository;

    //학과 정보에 대한 공통부분 조회 메서드
    public DepartmentInfoDTO getDepartmentInfo(UUID departmentUuid){
        Department department = departmentRepository.findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        return DepartmentInfoDTO.fromEntity(department);
    }

    // — 전체 학과 목록
    public List<DepartmentSummaryDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentSummaryDTO::fromEntity)
                .toList();
    }

    // — 단과대별 학과 목록
    public List<DepartmentSummaryDTO> getDepartmentsByCollege(College college) {
        return departmentRepository.findByCollege(college).stream()
                .map(DepartmentSummaryDTO::fromEntity)
                .toList();
    }
}
