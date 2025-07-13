package nova.mjs.domain.community.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.community.service.CommunityBoardImageService;
import nova.mjs.domain.community.service.CommunityBoardServiceImpl;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.s3.S3ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class CommunityBoardImageController {
    @Value("${s3.path.custom.board-temp}")
    private String boardTempPrefix;

    private final CommunityBoardServiceImpl communityBoardServiceImpl;
    private final CommunityBoardImageService communityBoardImageService;
    private final S3ServiceImpl s3ServiceImpl;

    // 1. 게시글 작성 시 사용할 tempUUID  발급
    @GetMapping("/temp-uuid")
    public ResponseEntity<String> generateTempUuid() {
        return ResponseEntity.ok(UUID.randomUUID().toString());
    }

    // 2. image 업로드
//    @PostMapping("/images")
//    public ResponseEntity<ApiResponse<String>> uploadImage(
//            @RequestParam() MultipartFile file,
//            @RequestParam() UUID tempFolderUuid) throws IOException {
//        String key = boardTempPrefix + tempFolderUuid + "/" + file.getOriginalFilename();
//
//        String imageUrl = s3Service.uploadFileAndGetUrl(file, key);
//
//        return ResponseEntity
//                .status(HttpStatus.OK).body(ApiResponse.success(imageUrl));
//    }

    @PostMapping("/images")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestParam MultipartFile file,
            @RequestParam UUID folderUUID) throws IOException {

        String imageUrl = communityBoardImageService.uploadCommunityBoardImage(file, folderUUID);

        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }


    // 3. 게시글 취소 시 temp 이미지 직접 삭제
    @DeleteMapping("/images")
    public ResponseEntity<Void> deleteTempImages(@RequestParam() UUID tempFolderUuid) {
        String prefix = boardTempPrefix + tempFolderUuid + "/";
        s3ServiceImpl.deleteFolder(prefix);
        return ResponseEntity.noContent().build();
    }
}

