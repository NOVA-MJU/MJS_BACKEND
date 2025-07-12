package nova.mjs.admin.department_schedule.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.admin.account.entity.StudentCouncilAdmin;
import nova.mjs.admin.account.exception.AdminIdMismatchException;
import nova.mjs.admin.account.repository.AdminRepository;
import nova.mjs.admin.department_schedule.dto.AdminDepartmentScheduleRequestDTO;
import nova.mjs.admin.department_schedule.dto.AdminDepartmentScheduleResponseDTO;
import nova.mjs.admin.department_schedule.repository.AdminDepartmentScheduleRepository;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.entity.DepartmentSchedule;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDepartmentScheduleService {
    private final AdminDepartmentScheduleRepository adminDepartmentScheduleRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public AdminDepartmentScheduleResponseDTO create(String adminId, AdminDepartmentScheduleRequestDTO request) {

        StudentCouncilAdmin studentCouncilAdmin = adminRepository.findByContactEmail(adminId)
                .orElseThrow(() -> {
                    log.warn("[일정 생성 실패] 존재하지 않는 adminId: {}", adminId);
                    return new AdminIdMismatchException();
                });

        DepartmentSchedule schedule = DepartmentSchedule.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .colorCode(request.getColorCode())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .department(studentCouncilAdmin.getDepartment())
                .departmentScheduleUuid(UUID.randomUUID())
                .build();

        DepartmentSchedule saved = adminDepartmentScheduleRepository.save(schedule);
        log.info("[학과 일정 등록 완료] id={}, title={}", saved.getId(), saved.getTitle());

        return AdminDepartmentScheduleResponseDTO.fromEntity(saved);
    }

    public List<AdminDepartmentScheduleResponseDTO> getSchedulesByMonth(String adminId, int year, int month) {
        StudentCouncilAdmin studentCouncilAdmin = adminRepository.findByContactEmail(adminId)
                .orElseThrow(AdminIdMismatchException::new);

        Department department = studentCouncilAdmin.getDepartment();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<DepartmentSchedule> schedules = adminDepartmentScheduleRepository
                .findAllByDepartmentAndStartDateBetween(department, start, end);

        return schedules.stream()
                .map(AdminDepartmentScheduleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
