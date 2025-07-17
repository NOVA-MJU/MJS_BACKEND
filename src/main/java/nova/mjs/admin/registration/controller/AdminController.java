package nova.mjs.admin.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nova.mjs.admin.registration.DTO.AdminDTO;
import nova.mjs.admin.registration.service.AdminCommandService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminCommandService adminCommandService;

    @PostMapping("/register")
    @PreAuthorize("isAuthenticated() and hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<String>> registerInitialAdmin(
            @RequestBody @Valid AdminDTO.StudentCouncilInitRegistrationRequestDTO request) {
        ApiResponse<String> response = adminCommandService.registerInitAdmin(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update")
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDTO.StudentCouncilResponseDTO>> updateAdmin(
            @RequestBody @Valid AdminDTO.StudentCouncilUpdateDTO request) {
        AdminDTO.StudentCouncilResponseDTO response = adminCommandService.updateAdmin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> validateAdminByEmail(
            @RequestParam("email") String email) {
        Boolean result = adminCommandService.validationInitAdminID(email);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
