package nova.mjs.admin.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.admin.DTO.AdminDTO;
import nova.mjs.admin.service.AdminService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.s3.S3Service;
import nova.mjs.util.security.AuthDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final S3Service s3Service;

    @Value("${s3.path.custom.admin-temp}")
    private String adminTempPrefix;

    // 1. Admin 회원가입시 작성 시 사용할 tempUUID  발급
    @GetMapping("/temp-uuid")
    public ResponseEntity<String> generateTempUuid() {
        return ResponseEntity.ok(UUID.randomUUID().toString());
    }


    @PostMapping("/images")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestParam MultipartFile file,
            @RequestParam UUID tempFolderUuid) throws IOException {

        String imageUrl = s3Service.uploadAdminLogo(file, tempFolderUuid);

        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }


    // 3. 회원가입하다가 말았을 때를 위한 temp 이미지 직접 삭제
    @DeleteMapping("/images")
    public ResponseEntity<Void> deleteTempImages(@RequestParam() UUID tempFolderUuid) {
        String prefix = adminTempPrefix + tempFolderUuid + "/";
        s3Service.deleteFolder(prefix);
        return ResponseEntity.noContent().build();
    }


    // 회원 가입
    @PostMapping
    public ResponseEntity<ApiResponse<?>> signUpAdmin(@RequestBody AdminDTO.AdminRequestDTO requestDTO) {
        AuthDTO.LoginResponseDTO newAdmin = adminService.createAdmin(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(newAdmin));
    }
}
