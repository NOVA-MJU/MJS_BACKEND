package nova.mjs.community.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.community.DTO.CommunityBoardRequest;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.community.service.CommunityBoardService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class CommunityBoardController {

    private final CommunityBoardService service;

    // 1. GET 페이지네이션
    @GetMapping
    public  ResponseEntity<ApiResponse<Page<CommunityBoardResponse>>> getBoards(
            @RequestParam(defaultValue = "0") int page, // 기본 페이지 번호
            @RequestParam(defaultValue = "10") int size // 기본 페이지 크기
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityBoardResponse> boards = service.getBoards(pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(boards));
    }


    // 2. GET 상세 content 조회
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CommunityBoardResponse>> getBoardDetail(@PathVariable UUID uuid) {
        CommunityBoardResponse board = service.getBoardDetail(uuid);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(board));
    }

    // 3. POST 게시글 작성
    @PostMapping
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.email.equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<CommunityBoardResponse>> createBoard(
            @RequestBody CommunityBoardRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        CommunityBoardResponse board = service.createBoard(request, userPrincipal.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(board)); // HTTP 201 Created
    }

    // 4. PATCH 게시글 수정
    @PatchMapping("/{uuid}")
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.email.equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<CommunityBoardResponse>> updateBoard(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CommunityBoardRequest request) {
        CommunityBoardResponse board = service.updateBoard(uuid, request, userPrincipal.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(board)); // HTTP 200 OK
    }

    // 5. DELETE 게시글 삭제
    @DeleteMapping("/{uuid}")
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.email.equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<Void> deleteBoard(@PathVariable UUID uuid) {
        service.deleteBoard(uuid);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
