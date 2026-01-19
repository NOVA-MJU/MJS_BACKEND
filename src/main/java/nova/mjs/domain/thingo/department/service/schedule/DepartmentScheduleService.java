package nova.mjs.domain.thingo.department.service.schedule;


import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.dto.DepartmentScheduleResponseDTO;
import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;
import nova.mjs.domain.thingo.department.repository.DepartmentScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentScheduleService {
    private final DepartmentScheduleRepository departmentScheduleRepository;

    public DepartmentScheduleResponseDTO getScheduleByDepartmentUuid(UUID departmentUuid){
        List<DepartmentSchedule> schedules = departmentScheduleRepository.findByDepartment_DepartmentUuid(departmentUuid);

        return DepartmentScheduleResponseDTO.fromScheduleList(schedules);
    }
}
