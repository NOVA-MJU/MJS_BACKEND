package nova.mjs.comments.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.comments.DTO.CommentsRequestDto;
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
    public ResponseEntity<ApiResponse<Page<CommentsResponseDto.CommentSummaryDto>>> getCommentsByBoard(
            @PathVariable UUID boardUUID,
            @RequestParam(defaultValue = "0") int page,   // 기본 페이지 번호
            @RequestParam(defaultValue = "10") int size   // 기본 페이지 크기

    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentsResponseDto.CommentSummaryDto> response = service.getCommentsByBoard(boardUUID, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 2. POSt 댓글 작성
    @PostMapping("/{communityBoardUuid}/member/{memberUuid}")
    public ResponseEntity<ApiResponse<CommentsResponseDto.CommentSummaryDto>> createComment(
            @PathVariable UUID communityBoardUuid,
            @PathVariable UUID memberUuid,
            @RequestBody CommentsRequestDto requestDto
            ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createComment(communityBoardUuid, requestDto.getContent(), memberUuid)));
    }

    // 3. DELETE 댓글 삭제
    @DeleteMapping("/{commentUUID}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentUUID) {
        service.deleteCommentByUuid(commentUUID);
        return ResponseEntity.noContent().build();
    }
}
