package nova.mjs.domain.department.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.department.DTO.DepartmentNoticesResponseDTO;
import nova.mjs.domain.department.DTO.DepartmentScheduleResponseDTO;
import nova.mjs.domain.department.DTO.DepartmentSummaryDTO;
import nova.mjs.domain.department.service.DepartmentNoticeService;
import nova.mjs.domain.department.service.DepartmentScheduleService;
import nova.mjs.domain.department.service.DepartmentService;
import nova.mjs.domain.member.entity.enumList.College;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentScheduleService departmentScheduleService;
    private final DepartmentNoticeService departmentNoticeService;

    @GetMapping("/{departmentUuid}/schedules")
    public ResponseEntity<ApiResponse<DepartmentScheduleResponseDTO>> getSchedules(
            @PathVariable UUID departmentUuid
    ){
        DepartmentScheduleResponseDTO scheduleResponse = departmentScheduleService.getScheduleBydepartmentUuid(departmentUuid);

        return ResponseEntity.ok(ApiResponse.success(scheduleResponse));
    }

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

    /** 1) 기본 5개씩 페이지네이션된 preview 리스트 */
    @GetMapping("/{departmentUuid}/notices")
    public ResponseEntity<ApiResponse<Page<DepartmentNoticesResponseDTO.NoticeSimpleDTO>>> getNotices(
            @PathVariable UUID departmentUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<DepartmentNoticesResponseDTO.NoticeSimpleDTO> dtoPage =
                departmentNoticeService.getNoticesPage(departmentUuid, page, size);
        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }

    /** 2) 토글 클릭 시 전체 content 반환 */
    @GetMapping("/{departmentUuid}/notices/{noticeUuid}")
    public ResponseEntity<ApiResponse<DepartmentNoticesResponseDTO.DepartmentNoticeDetailDTO>> getNoticeDetail(
            @PathVariable UUID departmentUuid,
            @PathVariable UUID noticeUuid
    ) {
        DepartmentNoticesResponseDTO.DepartmentNoticeDetailDTO dto = departmentNoticeService.getNoticeDetail(departmentUuid, noticeUuid);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

}
