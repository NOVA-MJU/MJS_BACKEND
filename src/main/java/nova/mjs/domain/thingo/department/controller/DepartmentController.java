package nova.mjs.domain.thingo.department.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.dto.DepartmentDTO;
import nova.mjs.domain.thingo.department.dto.DepartmentNoticeDTO;
import nova.mjs.domain.thingo.department.dto.StudentCouncilNoticeDTO;
import nova.mjs.domain.thingo.department.dto.DepartmentScheduleDTO;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.service.info.DepartmentInfoQueryService;
import nova.mjs.domain.thingo.department.service.notice.DepartmentNoticeQueryService;
import nova.mjs.domain.thingo.department.service.notice.StudentCouncilNoticeQueryService;
import nova.mjs.domain.thingo.department.service.schedule.DepartmentScheduleService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentInfoQueryService departmentInfoQueryService;
    private final DepartmentScheduleService departmentScheduleService;
    private final StudentCouncilNoticeQueryService studentCouncilNoticeQueryService;
    private final DepartmentNoticeQueryService departmentNoticeQueryService;

    /* ------------------------------------------------------------------
     *  학과 정보 (단건)
     * ------------------------------------------------------------------ */

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<DepartmentDTO.InfoResponse>> getInfo(
            @RequestParam College college,
            @RequestParam(required = false) DepartmentName department
    ) {
        DepartmentDTO.InfoResponse dto =
                departmentInfoQueryService.getDepartmentInfo(college, department);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /* ------------------------------------------------------------------
     *  학과 일정
     * ------------------------------------------------------------------ */

    @GetMapping("/schedules")
    public ResponseEntity<ApiResponse<DepartmentScheduleDTO.Response>> getSchedules(
            @RequestParam College college,
            @RequestParam(required = false) DepartmentName department
    ) {
        DepartmentScheduleDTO.Response dto =
                departmentScheduleService.getSchedule(college, department);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /* ------------------------------------------------------------------
     *  학생회 공지사항
     * ------------------------------------------------------------------ */

    @GetMapping("/student-council/notices")
    public ResponseEntity<ApiResponse<Page<StudentCouncilNoticeDTO.Summary>>> getNotices(
            @RequestParam College college,
            @RequestParam(required = false) DepartmentName department,
            @PageableDefault(page = 0, size = 5) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        studentCouncilNoticeQueryService.getNoticePage(
                                college,
                                department,
                                pageable
                        )
                )
        );
    }

    /* ==========================================================
     * 학생회 공지 상세
     *
     * 반드시 학과 정보와 함께 검증
     * ========================================================== */
    @GetMapping("/student-council/notices/{noticeUuid}")
    public ResponseEntity<ApiResponse<StudentCouncilNoticeDTO.Detail>> getNoticeDetail(
            @PathVariable UUID noticeUuid
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                studentCouncilNoticeQueryService.getNoticeDetail(
                        noticeUuid)
        ));
    }

    /* ==========================================================
     * 학과 공지사항
     * ========================================================== */

    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<Page<DepartmentNoticeDTO.Summary>>> getDepartmentNotices(
            @RequestParam College college,
            @RequestParam(required = false) DepartmentName department,
            @PageableDefault(page = 0, size = 5) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        departmentNoticeQueryService.getDepartmentNoticePage(
                                college,
                                department,
                                pageable
                        )
                )
        );
    }

    @PostMapping("/notices/crawl")
    public ResponseEntity<ApiResponse<Void>> crawlDepartmentNotices(
            @RequestParam(required = false) College college,
            @RequestParam(required = false) DepartmentName department
    ) {
        departmentNoticeQueryService.crawlDepartmentNotices(college, department);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
