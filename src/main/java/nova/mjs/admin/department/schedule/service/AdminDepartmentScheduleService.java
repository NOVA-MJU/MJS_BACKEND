package nova.mjs.admin.department.schedule.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.admin.account.exception.AdminIdMismatchWithDepartmentException;
import nova.mjs.admin.account.service.AdminQueryService;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleRequestDTO;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleResponseDTO;
import nova.mjs.admin.department.schedule.exception.DepartmentScheduleNotFoundException;
import nova.mjs.domain.thingo.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentScheduleRepository;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3Service;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 관리자용 학과 일정 서비스 (V2 기준)
 *
 * 비즈니스 규칙:
 *  - 학과 식별: College + DepartmentName
 *  - 관리자 검증: AdminQueryService 위임
 *  - 일정은 반드시 해당 학과 소속이어야 함
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminDepartmentScheduleService {

    private final AdminQueryService adminQueryService;
    private final DepartmentScheduleRepository scheduleRepository;
    private final DepartmentRepository departmentRepository;
    private final S3Service s3Service;

    private final String scheduleImagePrefix =
            S3DomainType.DEPARTMENT_SCHEDULE.getPrefix();

    /* ==========================================================
     * 1) 생성
     * ========================================================== */
    public AdminDepartmentScheduleResponseDTO createSchedule(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            AdminDepartmentScheduleRequestDTO dto
    ) {
        Department department = validateAdminAndGetDepartment(
                userPrincipal, college, departmentName
        );

        DepartmentSchedule schedule =
                DepartmentSchedule.create(dto, department);

        scheduleRepository.save(schedule);

        log.info("[학과 일정 생성] college={}, department={}, uuid={}",
                college, departmentName, schedule.getDepartmentScheduleUuid());

        return AdminDepartmentScheduleResponseDTO.fromEntity(schedule);
    }

    /* ==========================================================
     * 2) 수정
     * ========================================================== */
    public AdminDepartmentScheduleResponseDTO updateSchedule(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            UUID scheduleUuid,
            AdminDepartmentScheduleRequestDTO dto
    ) {
        Department department =
                validateAdminAndGetDepartment(userPrincipal, college, departmentName);

        DepartmentSchedule schedule =
                scheduleRepository
                        .findByDepartmentAndDepartmentScheduleUuid(department, scheduleUuid)
                        .orElseThrow(DepartmentScheduleNotFoundException::new);

        schedule.update(dto);

        log.info("[학과 일정 수정] college={}, department={}, uuid={}",
                college, departmentName, scheduleUuid);

        return AdminDepartmentScheduleResponseDTO.fromEntity(schedule);
    }

    /* ==========================================================
     * 3) 삭제
     * ========================================================== */
    public void deleteSchedule(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            UUID scheduleUuid
    ) {
        Department department =
                validateAdminAndGetDepartment(userPrincipal, college, departmentName);

        DepartmentSchedule schedule =
                scheduleRepository
                        .findByDepartmentAndDepartmentScheduleUuid(department, scheduleUuid)
                        .orElseThrow(DepartmentScheduleNotFoundException::new);

        // S3 이미지 삭제
        String folder = scheduleImagePrefix + scheduleUuid + "/";
        s3Service.deleteFolder(folder);

        scheduleRepository.delete(schedule);

        log.info("[학과 일정 삭제] college={}, department={}, uuid={}",
                college, departmentName, scheduleUuid);
    }

    /* ==========================================================
     * 공통 관리자 검증
     * ========================================================== */
    private Department validateAdminAndGetDepartment(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName
    ) {
        boolean isAdmin = adminQueryService
                .validateIsAdminOfDepartment(userPrincipal, college, departmentName);

        if (!isAdmin) {
            throw new AdminIdMismatchWithDepartmentException();
        }

        if (departmentName == null) {
            return departmentRepository
                    .findByCollegeAndDepartmentNameIsNull(college)
                    .orElseThrow(DepartmentNotFoundException::new);
        }

        return departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(DepartmentNotFoundException::new);
    }
}
