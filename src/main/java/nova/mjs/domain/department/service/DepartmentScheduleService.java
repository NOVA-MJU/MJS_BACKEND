package nova.mjs.domain.department.service;


import lombok.RequiredArgsConstructor;
import nova.mjs.domain.department.DTO.DepartmentScheduleResponseDTO;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.entity.DepartmentSchedule;
import nova.mjs.domain.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.department.repository.DepartmentRepository;
import nova.mjs.domain.department.repository.DepartmentScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentScheduleService {
    private final DepartmentScheduleRepository departmentScheduleRepository;
    private final DepartmentRepository departmentRepository;

    public DepartmentScheduleResponseDTO getScheduleBydepartmentUuid(UUID departmentUuid){
        Department department = departmentRepository.findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        List<DepartmentSchedule> schedules = departmentScheduleRepository.findByDepartment_DepartmentUuid(departmentUuid);

        return DepartmentScheduleResponseDTO.fromScheduleList(department, schedules);
    }
}
