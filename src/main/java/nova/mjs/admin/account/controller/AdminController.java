//package nova.mjs.admin.account.controller;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import nova.mjs.admin.account.DTO.AdminDTO;
//import nova.mjs.admin.account.DTO.LoginRequestDTO;
//import nova.mjs.admin.account.service.AdminService;
//import nova.mjs.util.response.ApiResponse;
//import nova.mjs.util.security.AuthDTO;
//import nova.mjs.util.security.UserPrincipal;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//@RestController
//@RequestMapping("/api/v1/admin")
//@RequiredArgsConstructor
//public class AdminController {
//    private final AdminService adminService;
//
//    // 사전 adminID 등록
//    @PostMapping("/pre-register")
//    public ResponseEntity<ApiResponse<?>> preRegisterAdminId(@RequestBody AdminDTO.AdminIdRequestDTO request) {
//        adminService.preRegisterAdminId(request.getAdminId());
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success("사전 등록 완료"));
//    }
//
//    // 사진 업로드 및 회원가입 데이터 저장
//    @PutMapping(value = "/{adminId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponse<?>> updateAdminInfo(
//            @PathVariable String adminId,
//            @Valid @RequestPart("data") AdminDTO.AdminRequestDTO dto,
//            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
//
//        adminService.updateAdminInfoWithImage(adminId, dto, file);
//        return ResponseEntity.ok(ApiResponse.success("업데이트 완료"));
//    }
//
//    // 비밀번호 저장
//    @PatchMapping("/{adminId}/password")
//    public ResponseEntity<ApiResponse<?>> updatePassword(
//            @PathVariable String adminId,
//            @RequestBody AdminDTO.PasswordRequestDTO request) {
//
//        adminService.updatePassword(adminId, request.getPassword());
//        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 저장되었습니다."));
//    }
//
//    // StudentCouncilAdmin Login
//    @PostMapping("/login")
//    public ResponseEntity<ApiResponse<AuthDTO.LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
//        AuthDTO.LoginResponseDTO response = adminService.   login(request.getAdminId(), request.getPassword());
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(ApiResponse.success(response));
//    }
//
//    // StudentCouncilAdmin 탈퇴
//    @DeleteMapping("/delete")
//    @PreAuthorize("isAuthenticated() and  hasRole('ADMIN')")
//    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@AuthenticationPrincipal UserPrincipal userPrincipal,
//                                                         @RequestBody AdminDTO.PasswordRequestDTO password) {
//        adminService.deleteAdmin(userPrincipal.getUsername(), password);
//        return ResponseEntity.ok(ApiResponse.success(null));
//    }
//}
