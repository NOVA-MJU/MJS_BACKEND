package nova.mjs.admin.department.schedule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleRequestDTO;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleResponseDTO;
import nova.mjs.admin.department.schedule.service.AdminDepartmentScheduleService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/department/{departmentUuid}/schedules")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
@Slf4j
public class AdminDepartmentScheduleController {

    private final AdminDepartmentScheduleService scheduleService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<AdminDepartmentScheduleResponseDTO>> createSchedule(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID departmentUuid,
            @RequestBody @Valid AdminDepartmentScheduleRequestDTO requestDTO) {

        return ResponseEntity.ok(
                ApiResponse.success(scheduleService.createSchedule(userPrincipal, departmentUuid, requestDTO))
        );
    }

    @PatchMapping("/{scheduleUuid}")
    public ResponseEntity<ApiResponse<AdminDepartmentScheduleResponseDTO>> updateSchedule(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID departmentUuid,
            @PathVariable UUID scheduleUuid,
            @RequestBody @Valid AdminDepartmentScheduleRequestDTO requestDTO) {

        return ResponseEntity.ok(
                ApiResponse.success(scheduleService.updateSchedule(userPrincipal, departmentUuid, scheduleUuid, requestDTO))
        );
    }

    @DeleteMapping("/{scheduleUuid}")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID departmentUuid,
            @PathVariable UUID scheduleUuid) {

        scheduleService.deleteSchedule(userPrincipal, departmentUuid, scheduleUuid);
        return ResponseEntity.ok(ApiResponse.success("학과 일정이 삭제되었습니다."));
    }
}
