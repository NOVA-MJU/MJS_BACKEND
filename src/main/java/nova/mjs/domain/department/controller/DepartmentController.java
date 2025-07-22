package nova.mjs.domain.department.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.department.dto.DepartmentNoticesDTO;
import nova.mjs.domain.department.dto.DepartmentScheduleResponseDTO;
import nova.mjs.domain.department.dto.DepartmentSummaryDTO;
import nova.mjs.domain.department.service.info.DepartmentInfoService;
import nova.mjs.domain.department.service.notice.DepartmentNoticeQueryService;
import nova.mjs.domain.department.service.schedule.DepartmentScheduleService;
import nova.mjs.domain.member.entity.enumList.College;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentInfoService departmentInfoService;
    private final DepartmentScheduleService departmentScheduleService;
    private final DepartmentNoticeQueryService departmentNoticeQueryService;


    /** ------------------------------------------------------------------
     *  학과 정보
     * ------------------------------------------------------------------ */


    // 학과 목록(전체 or 단과대별)
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentSummaryDTO>>> list(
            @RequestParam(required = false) College college) {

        List<DepartmentSummaryDTO> list = (college == null)
                ? departmentInfoService.getAllDepartments()
                : departmentInfoService.getDepartmentsByCollege(college);

        return ResponseEntity.ok(ApiResponse.success(list));
    }


    /** ------------------------------------------------------------------
     * 학과일정
     * ------------------------------------------------------------------ */

    // 학과 일정
    @GetMapping("/{departmentUuid}/schedules")
    public ResponseEntity<ApiResponse<DepartmentScheduleResponseDTO>> getSchedules(
            @PathVariable UUID departmentUuid) {
        DepartmentScheduleResponseDTO scheduleResponse = departmentScheduleService.getScheduleByDepartmentUuid(departmentUuid);
        return ResponseEntity.ok(ApiResponse.success(scheduleResponse));
    }


    /** ------------------------------------------------------------------
     *  공지사항
     * ------------------------------------------------------------------ */

    // 공지 목록(기본 size = 5)
    @GetMapping("/{departmentUuid}/notices")
    public ResponseEntity<ApiResponse<Page<DepartmentNoticesDTO.Summary>>> getNotices(
            @PathVariable UUID departmentUuid,
            @PageableDefault(page = 0, size = 5) Pageable pageable) {

        Page<DepartmentNoticesDTO.Summary> page =
                departmentNoticeQueryService.getNoticePage(departmentUuid, pageable);

        return ResponseEntity.ok(ApiResponse.success(page));
    }

    // 공지 상세
    @GetMapping("/{departmentUuid}/notices/{noticeUuid}")
    public ResponseEntity<ApiResponse<DepartmentNoticesDTO.Detail>> getNoticeDetail(
            @PathVariable UUID departmentUuid,
            @PathVariable UUID noticeUuid) {

        DepartmentNoticesDTO.Detail dto =
                departmentNoticeQueryService.getNoticeDetail(departmentUuid, noticeUuid);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
