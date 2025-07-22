package nova.mjs.util.s3;

import lombok.RequiredArgsConstructor;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3ServiceImpl s3Service;

    /**
     * 범용 S3 업로드 엔드포인트
     *
     * @param file 업로드할 파일
     * @param domain 업로드 도메인 (enum name)
     * @param uuid 연관된 엔티티 UUID
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam MultipartFile file,
            @RequestParam("domain") S3DomainType domain,
            @RequestParam("uuid") UUID uuid) throws IOException {

        String url = s3Service.uploadFile(file, domain, uuid);
        return ResponseEntity.ok(ApiResponse.success(url));
    }
}
