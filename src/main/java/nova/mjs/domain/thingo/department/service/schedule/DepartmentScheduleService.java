package nova.mjs.domain.thingo.department.service.schedule;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.dto.DepartmentScheduleDTO;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 학과 일정 조회 서비스 (읽기 전용)
 *
 * 비즈니스 규칙:
 *  - 학과는 College + DepartmentName으로 식별
 *  - 일정은 해당 학과에 속한 것만 조회 가능
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentScheduleService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentScheduleRepository scheduleRepository;

    /**
     * 학과 일정 전체 조회
     */
    public DepartmentScheduleDTO.Response getSchedule(
            College college,
            DepartmentName departmentName
    ) {
        Department department = departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(DepartmentNotFoundException::new);

        List<DepartmentSchedule> schedules =
                scheduleRepository.findByDepartment(department);

        return DepartmentScheduleDTO.Response.from(schedules);
    }
}
