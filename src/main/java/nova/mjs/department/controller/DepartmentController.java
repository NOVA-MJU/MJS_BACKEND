package nova.mjs.department.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.department.DTO.DepartmentNoticeDTO;
import nova.mjs.department.DTO.DepartmentNoticeResponseDTO;
import nova.mjs.department.DTO.DepartmentScheduleResponseDTO;
import nova.mjs.department.DTO.DepartmentSummaryDTO;
import nova.mjs.department.entity.enumList.College;
import nova.mjs.department.service.DepartmentScheduleService;
import nova.mjs.department.service.DepartmentService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nova.mjs.department.service.DepartmentNoticeService;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentScheduleService departmentScheduleService;
    private final DepartmentNoticeService departmentNoticeService;

    /** 1) 전체 or 단과대별 학과 목록 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentSummaryDTO>>> list(
            @RequestParam(required = false) College college
    ) {
        List<DepartmentSummaryDTO> dtoList = (college == null)
                ? departmentService.getAllDepartments()
                : departmentService.getDepartmentsByCollege(college);
        return ResponseEntity.ok(ApiResponse.success(dtoList));
    }
    @GetMapping("/{departmentUuid}/schedules")
    public ResponseEntity<ApiResponse<DepartmentScheduleResponseDTO>> getSchedules(
            @PathVariable UUID departmentUuid
    ){
        DepartmentScheduleResponseDTO scheduleResponse = departmentScheduleService.getScheduleBydepartmentUuid(departmentUuid);

        return ResponseEntity.ok(ApiResponse.success(scheduleResponse));
    }

    /** 3) 학과 공지사항 + Info */
    @GetMapping("/{departmentUuid}/notices")
    public ResponseEntity<ApiResponse<DepartmentNoticeResponseDTO>> notices(
            @PathVariable UUID departmentUuid
    ) {
        var dto = departmentNoticeService.getNoticesByDepartmentUuid(departmentUuid);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

}
