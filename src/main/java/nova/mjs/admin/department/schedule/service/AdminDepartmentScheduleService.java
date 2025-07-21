package nova.mjs.admin.department.schedule.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.admin.department.schedule.exception.DepartmentScheduleNotFoundException;
import nova.mjs.admin.account.exception.AdminIdMismatchException;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleRequestDTO;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleResponseDTO;
import nova.mjs.admin.department.schedule.repository.AdminDepartmentScheduleRepository;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.entity.DepartmentSchedule;
import nova.mjs.domain.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.department.repository.DepartmentRepository;
import nova.mjs.domain.department.repository.DepartmentScheduleRepository;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.repository.MemberRepository;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDepartmentScheduleService {
    private final AdminDepartmentScheduleRepository adminDepartmentScheduleRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentScheduleRepository departmentScheduleRepository;

    private final String scheduleImagePrefix = S3DomainType.DEPARTMENT_SCHEDULE.getPrefix();
    private final S3ServiceImpl s3ServiceImpl;

    //학과 일정 생성
    @Transactional
    public AdminDepartmentScheduleResponseDTO createSchedule(String adminEmail, UUID scheduleUuid, AdminDepartmentScheduleRequestDTO adminDepartmentScheduleRequestDTO) {
        //이메일을 받는 게 맞을까?

        Member admin = memberRepository.findByEmail(adminEmail)
                .orElseThrow(() -> {
                    log.warn("[일정 생성 실패] 존재하지 않는 admin 이메일 : {}", adminEmail);
                    return new AdminIdMismatchException();
                });

        //admin 확인
        if (admin.getRole() != Member.Role.ADMIN){
            log.warn("[일정 등록 실패] 권한 없음 : {}", adminEmail);
            throw new AdminIdMismatchException();
        }

        Department department = departmentRepository.findByAdminEmail(admin.getEmail())
                .orElseThrow(() -> {
                    log.warn("[일정 등록 실패] 해당 관리자에 연결된 학과 없음 : {}", adminEmail);
                    return new DepartmentNotFoundException();
                });

        DepartmentSchedule schedule = DepartmentSchedule.builder()
                .departmentScheduleUuid(scheduleUuid)
                .title(adminDepartmentScheduleRequestDTO.getTitle())
                .content(adminDepartmentScheduleRequestDTO.getContent())
                .colorCode(adminDepartmentScheduleRequestDTO.getColorCode())
                .startDate(adminDepartmentScheduleRequestDTO.getStartDate())
                .endDate(adminDepartmentScheduleRequestDTO.getEndDate())
                .department(department)
                .build();

        DepartmentSchedule saved = adminDepartmentScheduleRepository.save(schedule);

        return AdminDepartmentScheduleResponseDTO.fromEntity(saved);
    }

    //학과 일정 수정
    @Transactional
    public AdminDepartmentScheduleResponseDTO updateSchedule(String adminEmail, UUID scheduleUuid, AdminDepartmentScheduleRequestDTO adminDepartmentScheduleRequestDTO) {

        // 어드민 회원 조회 및 권한 검증
        Member admin = memberRepository.findByEmail(adminEmail)
                .orElseThrow(AdminIdMismatchException::new);

        if (admin.getRole() != Member.Role.ADMIN) {
            log.warn("[일정 수정 실패] 권한 없음: {}", adminEmail);
            throw new AdminIdMismatchException();
        }

        // 학과 정보 조회
        Department department = departmentRepository.findByAdminEmail(adminEmail)
                .orElseThrow(DepartmentNotFoundException::new);

        //일정 조회
        DepartmentSchedule schedule = departmentScheduleRepository.findByDepartmentScheduleUuid(scheduleUuid)
                .orElseThrow(DepartmentScheduleNotFoundException::new);

        //학과 불일치 예외
        if (!schedule.getDepartment().equals(department)){
            log.warn("[일정 수정 실패] 학과 불일치. 요청자 학과 : {}, 일정학과 : {}",
                    department.getDepartmentName(), schedule.getDepartment().getDepartmentName());
            throw new AdminIdMismatchException();
        }

        //학과 일정 수정
        schedule.updateFromRequest(adminDepartmentScheduleRequestDTO);
        log.info("[학과 일정 수정 완료] uuid : {}, title : {}", scheduleUuid, schedule.getTitle());

        return AdminDepartmentScheduleResponseDTO.fromEntity(schedule);
    }

    //학과 일정 삭제
    @Transactional
    public void deleteSchedule(String adminEmail, UUID scheduleUuid) {
        //admin 조회
        Member admin = memberRepository.findByEmail(adminEmail)
                .orElseThrow(() -> {
                    log.warn("[일정 삭제 실패] 존재하지 않는 이메일: {}", adminEmail);
                    return new AdminIdMismatchException();
                });

        //admin 권한 확인
        if (admin.getRole() != Member.Role.ADMIN) {
            log.warn("[일정 삭제 실패] 권한 없음: {}", adminEmail);
            throw new AdminIdMismatchException();
        }

        //admin 관리 학과 조회
        Department department = departmentRepository.findByAdminEmail(adminEmail)
                .orElseThrow(() -> {
                    log.warn("[일정 삭제 실패] 학과 정보 없음: {}", adminEmail);
                    return new DepartmentNotFoundException();
                });

        // 삭제할 일정 조회
        DepartmentSchedule schedule = departmentScheduleRepository.findByDepartmentScheduleUuid(scheduleUuid)
                .orElseThrow(() -> {
                    log.warn("[일정 삭제 실패] 존재하지 않는 일정: uuid={}", scheduleUuid);
                    return new DepartmentScheduleNotFoundException();
                });

        //학과 일치 여부 검증
        if (!schedule.getDepartment().equals(department)){
            log.warn("[일정 삭제 실패] 학과 불일치. 요청자 학과 : {}, 일정 학과 : {}",
                    department.getDepartmentName(), schedule.getDepartment().getDepartmentName());
            throw new AdminIdMismatchException();
        }

        String scheduleFolder = scheduleImagePrefix + scheduleUuid + "/";
        s3ServiceImpl.deleteFolder(scheduleFolder);
        log.info("[S3 이미지 삭제 완료 {} :", scheduleFolder);


        //일정 삭제
        departmentScheduleRepository.delete(schedule);
        log.info("[일정 삭제 완료] uuid : {}", scheduleUuid);
    }

}
