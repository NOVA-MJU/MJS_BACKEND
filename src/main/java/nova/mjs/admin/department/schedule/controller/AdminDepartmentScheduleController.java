package nova.mjs.admin.department.schedule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleRequestDTO;
import nova.mjs.admin.department.schedule.dto.AdminDepartmentScheduleResponseDTO;
import nova.mjs.admin.department.schedule.service.AdminDepartmentScheduleService;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/departments/schedules")
public class AdminDepartmentScheduleController {

    private final AdminDepartmentScheduleService service;

    @PostMapping
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<AdminDepartmentScheduleResponseDTO>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam College college,
            @RequestParam DepartmentName department,
            @RequestBody @Valid AdminDepartmentScheduleRequestDTO request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        service.createSchedule(userPrincipal, college, department, request)
                )
        );
    }

    @PatchMapping("/{scheduleUuid}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<AdminDepartmentScheduleResponseDTO>> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam College college,
            @RequestParam DepartmentName department,
            @PathVariable UUID scheduleUuid,
            @RequestBody @Valid AdminDepartmentScheduleRequestDTO request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        service.updateSchedule(userPrincipal, college, department, scheduleUuid, request)
                )
        );
    }

    @DeleteMapping("/{scheduleUuid}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<String>> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam College college,
            @RequestParam DepartmentName department,
            @PathVariable UUID scheduleUuid
    ) {
        service.deleteSchedule(userPrincipal, college, department, scheduleUuid);
        return ResponseEntity.ok(ApiResponse.success("삭제 완료"));
    }
}
