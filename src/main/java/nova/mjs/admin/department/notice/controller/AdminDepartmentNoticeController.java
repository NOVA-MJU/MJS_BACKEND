package nova.mjs.admin.department.notice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeRequestDTO;
import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeResponseDTO;
import nova.mjs.admin.department.notice.service.AdminDepartmentNoticeService;
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
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(
                ApiResponse.success(
                    service.getAdminDepartmentNoticeDetail(noticeUuid, departmentUuid, userPrincipal)
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminDepartmentNoticeResponseDTO>> create(
            @PathVariable UUID departmentUuid,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid AdminDepartmentNoticeRequestDTO requestDto
    ) {
        AdminDepartmentNoticeResponseDTO created = service.createNotice(requestDto, departmentUuid, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    @PatchMapping("/{noticeUuid}")
    public ResponseEntity<ApiResponse<AdminDepartmentNoticeResponseDTO>> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID departmentUuid,
            @PathVariable UUID noticeUuid,
            @RequestBody @Valid AdminDepartmentNoticeRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            service.updateNotice(userPrincipal, departmentUuid, noticeUuid, requestDTO)
        ));
    }

    @DeleteMapping("/{noticeUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable UUID departmentUuid,
            @PathVariable UUID noticeUuid,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        service.deleteNotice(noticeUuid, departmentUuid, userPrincipal);
        return ResponseEntity.ok().body(ApiResponse.success("삭제가 성공적으로 완료되었습니다."));
    }
}