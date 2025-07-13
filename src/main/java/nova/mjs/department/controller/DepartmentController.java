package nova.mjs.department.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.department.DTO.DepartmentScheduleResponseDTO;
import nova.mjs.department.service.DepartmentScheduleService;
import nova.mjs.department.service.DepartmentService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentScheduleService departmentScheduleService;

    @GetMapping("/{departmentUuid}/schedules")
    public ResponseEntity<ApiResponse<DepartmentScheduleResponseDTO>> getSchedules(
            @PathVariable UUID departmentUuid
    ){
        DepartmentScheduleResponseDTO scheduleResponse = departmentScheduleService.getScheduleBydepartmentUuid(departmentUuid);

        return ResponseEntity.ok(ApiResponse.success(scheduleResponse));
    }
}
