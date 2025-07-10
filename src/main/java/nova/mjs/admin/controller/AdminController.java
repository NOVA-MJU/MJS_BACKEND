package nova.mjs.admin.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.admin.DTO.AdminDTO;
import nova.mjs.admin.DTO.PasswordRequestDTO;
import nova.mjs.admin.service.AdminService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.s3.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/pre-register")
    public ResponseEntity<ApiResponse<?>> preRegisterAdminId(@RequestParam String adminId) {
        adminService.preRegisterAdminId(adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("사전 등록 완료"));
    }

    @PutMapping(value = "/{adminId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateAdminInfo(
            @PathVariable String adminId,
            @RequestPart("data") AdminDTO.AdminRequestDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        adminService.updateAdminInfoWithImage(adminId, dto, file);
        return ResponseEntity.ok(ApiResponse.success("업데이트 완료"));
    }

    @PutMapping("/{adminId}/password")
    public ResponseEntity<ApiResponse<?>> updatePassword(
            @PathVariable String adminId,
            @RequestBody PasswordRequestDTO request) {

        adminService.updatePassword(adminId, request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 저장되었습니다."));
    }

}
