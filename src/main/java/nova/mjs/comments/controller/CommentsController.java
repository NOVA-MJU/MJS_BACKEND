package nova.mjs.comments.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.comments.DTO.CommentsRequestDto;
import nova.mjs.comments.DTO.CommentsResponseDto;
import nova.mjs.comments.service.CommentsService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class CommentsController {
    private final CommentsService service;

    // 1. 특정 게시물 댓글 목록 조회
    @GetMapping("/{boardUUID}/comments")
    public ResponseEntity<ApiResponse<List<CommentsResponseDto.CommentSummaryDto>>> getCommentsByBoard(
            @PathVariable UUID boardUUID) {
        List<CommentsResponseDto.CommentSummaryDto> response = service.getCommentsByBoard(boardUUID);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 2. POSt 댓글 작성
    @PostMapping("/{boardUUID}/comments")
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.email.equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<CommentsResponseDto.CommentSummaryDto>> createComment(
            @PathVariable UUID boardUUID,
            @AuthenticationPrincipal UserPrincipal userPrincipal, // 로그인 해야만 댓글 작성 가능
            @RequestBody CommentsRequestDto requestDto
            ) {
        CommentsResponseDto.CommentSummaryDto response = service.createComment(boardUUID, requestDto.getContent(), userPrincipal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 3. DELETE 댓글 삭제
    @DeleteMapping("/comments/{commentUUID}")
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.email.equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentUUID,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        service.deleteCommentByUuid(commentUUID, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
