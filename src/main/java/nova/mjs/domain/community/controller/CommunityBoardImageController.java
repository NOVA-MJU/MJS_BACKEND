package nova.mjs.domain.community.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.community.service.CommunityBoardImageService;
import nova.mjs.domain.community.service.CommunityBoardService;
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

    private final CommunityBoardService communityBoardServiceImpl;
    private final CommunityBoardImageService communityBoardImageService;
    private final S3ServiceImpl s3ServiceImpl;

    // 1. 게시글 작성 시 사용할 tempUUID  발급
    @GetMapping("/temp-uuid")
    public ResponseEntity<ApiResponse<String>> generateTempUuid() {
        return ResponseEntity.ok(ApiResponse.success(UUID.randomUUID().toString()));
    }

    // 2. image 업로드
    @PostMapping("/images")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestParam MultipartFile file,
            @RequestParam UUID boardUuid) throws IOException {

        String imageUrl = communityBoardImageService.uploadCommunityBoardImage(file, boardUuid);

        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }


//    @Deprecated
//    @PostMapping("/images")
//    public ResponseEntity<ApiResponse<String>> uploadImage(
//            @RequestParam() MultipartFile file,
//            @RequestParam() UUID tempboardUuid) throws IOException {
//        String key = boardTempPrefix + tempboardUuid + "/" + file.getOriginalFilename();
//
//        String imageUrl = s3Service.uploadFileAndGetUrl(file, key);
//
//        return ResponseEntity
//                .status(HttpStatus.OK).body(ApiResponse.success(imageUrl));
//    }

//    // 3. 게시글 취소 시 temp 이미지 직접 삭제
//    @Deprecated
//    @DeleteMapping("/images")
//    public ResponseEntity<Void> deleteTempImages(@RequestParam() UUID tempboardUuid) {
//        String prefix = boardTempPrefix + tempboardUuid + "/";
//        s3ServiceImpl.deleteFolder(prefix);
//        return ResponseEntity.noContent().build();
//    }
}

