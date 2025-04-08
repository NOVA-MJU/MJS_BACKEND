package nova.mjs.community.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.community.DTO.CommunityBoardRequest;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.community.service.CommunityBoardService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.s3.S3Service;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class CommunityBoardController {

    private final CommunityBoardService communityBoardService;
    private final S3Service s3Service;

    // 1. GET 페이지네이션
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommunityBoardResponse>>> getBoards(
            @RequestParam(defaultValue = "0") int page, // 기본 페이지 번호
            @RequestParam(defaultValue = "10") int size, // 기본 페이지 크기
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // 로그인 사용자 이메일 추출
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityBoardResponse> boards = communityBoardService.getBoards(pageable, email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(boards));
    }


    // 2. GET 상세 content 조회
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CommunityBoardResponse>> getBoardDetail(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;
        CommunityBoardResponse board = communityBoardService.getBoardDetail(uuid, email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(board));
    }

    // 3. POST 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<CommunityBoardResponse>> createBoard(
            @RequestBody CommunityBoardRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;
        CommunityBoardResponse board = communityBoardService.createBoard(request, email);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(board)); // HTTP 201 Created
    }

    // 4. PATCH 게시글 수정 (기존 이미지 + 새로운 이미지 비교)
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CommunityBoardResponse>> updateBoard(
            @PathVariable UUID uuid,
            @RequestBody CommunityBoardRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal // ★추가
    ) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;
        CommunityBoardResponse board = communityBoardService.updateBoard(uuid, request, email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(board)); // HTTP 200 OK
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // 비로그인 상태일 수도 있으므로 체크
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        // 서비스에 게시글 UUID와 사용자 email을 넘김
        communityBoardService.deleteBoard(uuid, email);

        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
