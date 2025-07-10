package nova.mjs.admin.department_schedule.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.admin.department_schedule.dto.DepartmentScheduleRequestDTO;
import nova.mjs.admin.department_schedule.dto.DepartmentScheduleResponseDTO;
import nova.mjs.admin.department_schedule.service.DepartmentScheduleService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/department-schedules")
@RequiredArgsConstructor
public class DepartmentScheduleController {

    private final DepartmentScheduleService scheduleService;

    // 학과 일정 등록
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentScheduleResponseDTO>> createSchedule(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody DepartmentScheduleRequestDTO request
    ) {
        DepartmentScheduleResponseDTO result = scheduleService.create(userPrincipal.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result));
    }

    // 해당 월의 일정 조회
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentScheduleResponseDTO>>> getSchedulesByMonth(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<DepartmentScheduleResponseDTO> schedules =
                scheduleService.getSchedulesByMonth(userPrincipal.getUsername(), year, month);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    // 일정 수정


    // 일정 삭제
}
