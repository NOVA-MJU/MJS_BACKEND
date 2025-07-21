package nova.mjs.admin.department.schedule.controller;

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
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/department-schedules")
@RequiredArgsConstructor
public class AdminDepartmentScheduleController {

    private final AdminDepartmentScheduleService scheduleService;

    //일정 생성
    @PostMapping("/{scheduleUuid}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<AdminDepartmentScheduleResponseDTO>> createSchedule(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID scheduleUuid,
            @RequestBody AdminDepartmentScheduleRequestDTO requestDTO){

        AdminDepartmentScheduleResponseDTO responseDTO = scheduleService.createSchedule(userPrincipal.getEmail(), scheduleUuid, requestDTO);
        return ResponseEntity
                .ok(ApiResponse.success(responseDTO));
    }

    // 일정 수정
    @PatchMapping("/{scheduleUuid}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<AdminDepartmentScheduleResponseDTO>> updateSchedule(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID scheduleUuid,
            @RequestBody AdminDepartmentScheduleRequestDTO requestDTO){

        AdminDepartmentScheduleResponseDTO updatedResultDTO = scheduleService.updateSchedule(userPrincipal.getEmail(), scheduleUuid, requestDTO);
        return ResponseEntity
                .ok(ApiResponse.success(updatedResultDTO));
    }

    // 일정 삭제
    @DeleteMapping("/{scheduleUuid}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID scheduleUuid){

        scheduleService.deleteSchedule(userPrincipal.getEmail(), scheduleUuid);
        return ResponseEntity
                .ok(ApiResponse.success("학과 일정이 삭제되었습니다."));
    }
}
