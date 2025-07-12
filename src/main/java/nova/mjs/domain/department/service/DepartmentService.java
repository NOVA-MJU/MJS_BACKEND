package nova.mjs.domain.department.service;


import lombok.RequiredArgsConstructor;
import nova.mjs.domain.department.DTO.DepartmentInfoDTO;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.department.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    //학과 정보에 대한 공통부분 조회 메서드
    public DepartmentInfoDTO getDepartmentInfo(UUID departmentUuid){
        Department department = departmentRepository.findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        return DepartmentInfoDTO.fromDepartmentEntity(department);
    }
}
