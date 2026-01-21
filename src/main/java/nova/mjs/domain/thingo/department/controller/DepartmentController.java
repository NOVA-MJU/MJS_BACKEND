package nova.mjs.domain.thingo.department.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.dto.DepartmentInfoDTO;
import nova.mjs.domain.thingo.department.dto.DepartmentNoticesDTO;
import nova.mjs.domain.thingo.department.dto.DepartmentScheduleResponseDTO;
import nova.mjs.domain.thingo.department.dto.DepartmentSummaryDTO;
import nova.mjs.domain.thingo.department.service.info.DepartmentInfoQueryService;
import nova.mjs.domain.thingo.department.service.notice.DepartmentNoticeQueryService;
import nova.mjs.domain.thingo.department.service.schedule.DepartmentScheduleService;
import nova.mjs.domain.thingo.member.entity.enumList.College;
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

    private final DepartmentInfoQueryService departmentInfoQueryService;
    private final DepartmentScheduleService departmentScheduleService;
    private final DepartmentNoticeQueryService departmentNoticeQueryService;


    /** ------------------------------------------------------------------
     *  학과 정보
     * ------------------------------------------------------------------ */


    // 학과 목록(전체 or 단과대별)
    @GetMapping("info")
    public ResponseEntity<ApiResponse<List<DepartmentSummaryDTO>>> list(
            @RequestParam(required = false) College college) {

        List<DepartmentSummaryDTO> list = (college == null)
                ? departmentInfoQueryService.getAllDepartments()
                : departmentInfoQueryService.getDepartmentsByCollege(college);

        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // 학과 별 상세 정보
    @GetMapping("info/{departmentUuid}")
    public ResponseEntity<ApiResponse<DepartmentInfoDTO>> list(
            @PathVariable UUID departmentUuid) {
        DepartmentInfoDTO departmentInfoDTO = departmentInfoQueryService.getDepartmentInfo(departmentUuid);
        return ResponseEntity.ok(ApiResponse.success(departmentInfoDTO));
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
