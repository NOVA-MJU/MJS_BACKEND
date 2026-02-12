package nova.mjs.admin.department.notice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeRequestDTO;
import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeResponseDTO;
import nova.mjs.admin.department.notice.service.AdminDepartmentNoticeService;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 관리자용 학과 공지사항 컨트롤러 (V2 기준)
 *
 * 학과 식별: College + DepartmentName
 */
@RestController
@RequestMapping("/api/v1/admin/departments/notices")
@RequiredArgsConstructor
public class AdminDepartmentNoticeController {

    private final AdminDepartmentNoticeService service;

    /* ==========================================================
     * 상세 조회
     * ========================================================== */
    @GetMapping("/{noticeUuid}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<AdminDepartmentNoticeResponseDTO>> getDetail(
            @RequestParam College college,
            @RequestParam DepartmentName department,
            @PathVariable UUID noticeUuid,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        service.getAdminDepartmentNoticeDetail(
                                college,
                                department,
                                noticeUuid,
                                userPrincipal
                        )
                )
        );
    }

    /* ==========================================================
     * 생성
     * ========================================================== */
    @PostMapping
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<AdminDepartmentNoticeResponseDTO>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam College college,
            @RequestParam DepartmentName department,
            @RequestBody @Valid AdminDepartmentNoticeRequestDTO requestDto
    ) {
        AdminDepartmentNoticeResponseDTO created =
                service.createNotice(userPrincipal, college, department, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    /* ==========================================================
     * 수정
     * ========================================================== */
    @PatchMapping("/{noticeUuid}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<AdminDepartmentNoticeResponseDTO>> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam College college,
            @RequestParam DepartmentName department,
            @PathVariable UUID noticeUuid,
            @RequestBody @Valid AdminDepartmentNoticeRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        service.updateNotice(
                                userPrincipal,
                                college,
                                department,
                                noticeUuid,
                                requestDTO
                        )
                )
        );
    }

    /* ==========================================================
     * 삭제
     * ========================================================== */
    @DeleteMapping("/{noticeUuid}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<String>> delete(
            @RequestParam College college,
            @RequestParam DepartmentName department,
            @PathVariable UUID noticeUuid,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        service.deleteNotice(userPrincipal, college, department, noticeUuid);

        return ResponseEntity.ok(
                ApiResponse.success("삭제가 성공적으로 완료되었습니다.")
        );
    }
}
