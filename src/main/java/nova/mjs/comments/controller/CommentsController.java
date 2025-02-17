package nova.mjs.comments.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.comments.DTO.CommentsResponseDto;
import nova.mjs.comments.service.CommentsService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentsController {
    private final CommentsService service;

    // 1. 특정 게시물 댓글 목록 조회
    @GetMapping("/board/{boardUUID}")
    public ResponseEntity<ApiResponse<Page<CommentsResponseDto>>> getCommentsByBoard(
            @PathVariable UUID boardUUID,
            @RequestParam(defaultValue = "0") int page,   // 기본 페이지 번호
            @RequestParam(defaultValue = "10") int size   // 기본 페이지 크기

    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentsResponseDto> response = service.getCommentsByBoard(boardUUID, pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    // 2. 단일 댓글 조회
    @GetMapping("/{commentUUID}")
    public ResponseEntity<ApiResponse<CommentsResponseDto>> getComment(@PathVariable UUID commentUUID) {
        CommentsResponseDto response = service.getCommentByUuid(commentUUID);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    // 3. POSt 댓글 작성
    @PostMapping("/{communityBoardUuid}/member/{memberUuid}")
    public ResponseEntity<ApiResponse<CommentsResponseDto>> createComment(
            @PathVariable UUID communityBoardUuid,
            @PathVariable UUID memberUuid,
            @RequestBody CommentsResponseDto request
    ) {
        CommentsResponseDto comment = service.createComment(communityBoardUuid, request, memberUuid);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(comment));
    }

    // 4. DELETE 댓글 삭제
    @DeleteMapping("/{commentUUID}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentUUID) {
        service.deleteCommentByUuid(commentUUID);
        return ResponseEntity.noContent().build();
    }
}
