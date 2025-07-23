package nova.mjs.admin.department.schedule.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.admin.account.exception.AdminIdMismatchWithDepartmentException;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleRequestDTO;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleResponseDTO;
import nova.mjs.admin.department.schedule.exception.DepartmentScheduleNotFoundException;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.entity.DepartmentSchedule;
import nova.mjs.domain.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.department.repository.DepartmentRepository;
import nova.mjs.domain.department.repository.DepartmentScheduleRepository;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3ServiceImpl;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDepartmentScheduleService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentScheduleRepository scheduleRepository;
    private final S3ServiceImpl s3ServiceImpl;

    private final String scheduleImagePrefix = S3DomainType.DEPARTMENT_SCHEDULE.getPrefix();

    /**
     * 학과 일정 생성
     */
    @Transactional
    public AdminDepartmentScheduleResponseDTO createSchedule(UserPrincipal userPrincipal, UUID departmentUuid, UUID scheduleUuid, AdminDepartmentScheduleRequestDTO dto) {
        Department department = getVerifiedDepartment(userPrincipal, departmentUuid);
        DepartmentSchedule schedule = DepartmentSchedule.create(scheduleUuid, dto, department);
        return AdminDepartmentScheduleResponseDTO.fromEntity(scheduleRepository.save(schedule));
    }

    /**
     * 학과 일정 수정
     */
    @Transactional
    public AdminDepartmentScheduleResponseDTO updateSchedule(UserPrincipal userPrincipal, UUID departmentUuid, UUID scheduleUuid, AdminDepartmentScheduleRequestDTO dto) {
        Department department = getVerifiedDepartment(userPrincipal, departmentUuid);
        DepartmentSchedule schedule = getVerifiedSchedule(scheduleUuid);

        if (!schedule.getDepartment().equals(department)) {
            log.warn("[일정 수정 실패] 학과 불일치. 요청자 학과 : {}, 일정 학과 : {}", department.getDepartmentName(), schedule.getDepartment().getDepartmentName());
            throw new AdminIdMismatchWithDepartmentException();
        }

        schedule.update(dto);
        log.info("[학과 일정 수정 완료] uuid : {}, title : {}", scheduleUuid, schedule.getTitle());
        return AdminDepartmentScheduleResponseDTO.fromEntity(schedule);
    }

    /**
     * 학과 일정 삭제
     */
    @Transactional
    public void deleteSchedule(UserPrincipal userPrincipal, UUID departmentUuid, UUID scheduleUuid) {
        Department department = getVerifiedDepartment(userPrincipal, departmentUuid);
        DepartmentSchedule schedule = getVerifiedSchedule(scheduleUuid);

        if (!schedule.getDepartment().equals(department)) {
            log.warn("[일정 삭제 실패] 학과 불일치. 요청자 학과 : {}, 일정 학과 : {}", department.getDepartmentName(), schedule.getDepartment().getDepartmentName());
            throw new AdminIdMismatchWithDepartmentException();
        }

        String scheduleFolder = scheduleImagePrefix + scheduleUuid + "/";
        s3ServiceImpl.deleteFolder(scheduleFolder);
        log.info("[S3 이미지 삭제 완료] 경로 : {}", scheduleFolder);

        scheduleRepository.delete(schedule);
        log.info("[일정 삭제 완료] uuid : {}", scheduleUuid);
    }

    /**
     * 현재 유저가 관리자인 학과 정보 검증 및 반환
     */
    private Department getVerifiedDepartment(UserPrincipal userPrincipal, UUID departmentUuid) {
        Department department = departmentRepository.findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        if (!department.getAdmin().getUuid().equals(userPrincipal.getUuid())) {
            log.warn("[권한 오류] 해당 학과의 관리자가 아님. 요청자 email: {}, 학과 UUID: {}", userPrincipal.getUsername(), departmentUuid);
            throw new AdminIdMismatchWithDepartmentException();
        }
        return department;
    }

    /**
     * 일정 UUID로 존재 여부 확인 및 반환
     */
    private DepartmentSchedule getVerifiedSchedule(UUID scheduleUuid) {
        return scheduleRepository.findByDepartmentScheduleUuid(scheduleUuid)
                .orElseThrow(DepartmentScheduleNotFoundException::new);
    }
}
