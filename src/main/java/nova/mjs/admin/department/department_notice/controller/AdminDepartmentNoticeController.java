package nova.mjs.admin.department.department_notice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nova.mjs.admin.department.department_notice.dto.AdminDepartmentNoticeRequestDTO;
import nova.mjs.admin.department.department_notice.dto.AdminDepartmentNoticeResponseDTO;
import nova.mjs.admin.department.department_notice.service.AdminDepartmentNoticeService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/departments/{departmentUuid}/notices")
@RequiredArgsConstructor
public class AdminDepartmentNoticeController {

    private final AdminDepartmentNoticeService service;

    @GetMapping("/{noticeUuid}")
    public ResponseEntity<ApiResponse<AdminDepartmentNoticeResponseDTO>> getDetail(
            @PathVariable UUID departmentUuid,
            @PathVariable UUID noticeUuid,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        service.getAdminDepartmentNoticeDetail(noticeUuid, departmentUuid, user)
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminDepartmentNoticeResponseDTO>> create(
            @PathVariable UUID departmentUuid,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody @Valid AdminDepartmentNoticeRequestDTO dto
    ) {
        AdminDepartmentNoticeResponseDTO created = service.createNotice(dto, departmentUuid, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    @PatchMapping("/{noticeUuid}")
    public ResponseEntity<ApiResponse<AdminDepartmentNoticeResponseDTO>> update(
            @PathVariable UUID departmentUuid,
            @PathVariable UUID noticeUuid,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody @Valid AdminDepartmentNoticeRequestDTO dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        service.updateNotice(noticeUuid, dto, departmentUuid, user)
                )
        );
    }

    @DeleteMapping("/{noticeUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID departmentUuid,
            @PathVariable UUID noticeUuid,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        service.deleteNotice(noticeUuid, departmentUuid, user);
    }
}