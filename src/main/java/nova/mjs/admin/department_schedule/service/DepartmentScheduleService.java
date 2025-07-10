package nova.mjs.admin.department_schedule.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.admin.account.entity.Admin;
import nova.mjs.admin.account.exception.AdminIdMismatchException;
import nova.mjs.admin.account.repository.AdminRepository;
import nova.mjs.admin.department_schedule.dto.DepartmentScheduleRequestDTO;
import nova.mjs.admin.department_schedule.dto.DepartmentScheduleResponseDTO;
import nova.mjs.admin.department_schedule.entity.DepartmentSchedule;
import nova.mjs.admin.department_schedule.repository.DepartmentScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentScheduleService {
    private final DepartmentScheduleRepository departmentScheduleRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public DepartmentScheduleResponseDTO create(String adminId, DepartmentScheduleRequestDTO request) {

        Admin admin = adminRepository.findByAdminId(adminId)
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
                .admin(admin)
                .build();

        DepartmentSchedule saved = departmentScheduleRepository.save(schedule);
        log.info("[학과 일정 등록 완료] id={}, title={}", saved.getId(), saved.getTitle());

        return DepartmentScheduleResponseDTO.fromEntity(saved);
    }

    public List<DepartmentScheduleResponseDTO> getSchedulesByMonth(String adminId, int year, int month) {
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(AdminIdMismatchException::new);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<DepartmentSchedule> schedules = departmentScheduleRepository
                .findAllByAdminAndStartDateBetween(admin, start, end);

        return schedules.stream()
                .map(DepartmentScheduleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
