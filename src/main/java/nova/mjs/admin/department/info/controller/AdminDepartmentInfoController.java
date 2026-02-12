package nova.mjs.admin.department.info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nova.mjs.admin.department.info.service.AdminDepartmentCommandService;
import nova.mjs.domain.thingo.department.dto.DepartmentDTO;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 학과 정보 관리 컨트롤러
 *
 * 정책:
 * - College는 필수
 * - DepartmentName은 선택
 * - (college, departmentName) 조합은 유니크
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/departments")
public class AdminDepartmentInfoController {

    private final AdminDepartmentCommandService service;

    /* =========================
     * 생성
     * ========================= */
    @PostMapping
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<DepartmentDTO.InfoResponse>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid DepartmentDTO.CreateRequest request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        service.createDepartment(userPrincipal, request)
                ));
    }

    /* =========================
     * 수정
     * ========================= */
    @PatchMapping
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<DepartmentDTO.InfoResponse>> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam College college,
            @RequestParam(required = false) DepartmentName departmentName,
            @RequestBody @Valid DepartmentDTO.UpdateRequest request
    ) {

        DepartmentDTO.InfoResponse response =service.updateDepartment(
                userPrincipal,
                college,
                departmentName,
                request
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /* =========================
     * 삭제
     * ========================= */
    @DeleteMapping
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<String>> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam College college,
            @RequestParam(required = false) DepartmentName departmentName
    ) {

        service.deleteDepartment(userPrincipal, college, departmentName);

        return ResponseEntity.ok(
                ApiResponse.success("학과 삭제가 완료되었습니다.")
        );
    }
}
