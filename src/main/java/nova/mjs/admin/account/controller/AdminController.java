package nova.mjs.admin.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nova.mjs.admin.account.DTO.AdminDTO;
import nova.mjs.admin.account.service.AdminCommandService;
import nova.mjs.domain.thingo.department.dto.DepartmentDTO;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/account")
@RequiredArgsConstructor
public class AdminController {

    private final AdminCommandService adminCommandService;

    @PostMapping("/register")
    @PreAuthorize("isAuthenticated() and hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<String>> registerInitialAdmin(
            @Valid @RequestBody AdminDTO.StudentCouncilInitRegistrationRequestDTO request) {

        ApiResponse<String> response = adminCommandService.registerInitAdmin(request);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/update")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<AdminDTO.StudentCouncilResponseDTO>> updateAdmin(
            @RequestBody @Validated AdminDTO.StudentCouncilUpdateDTO request) {
        AdminDTO.StudentCouncilResponseDTO response = adminCommandService.updateAdmin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or hasRole('OPERATOR'))")
    public ResponseEntity<ApiResponse<Boolean>> validateAdminByEmail(
            @RequestParam("email") String email) {
        Boolean result = adminCommandService.validationInitAdminID(email);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
