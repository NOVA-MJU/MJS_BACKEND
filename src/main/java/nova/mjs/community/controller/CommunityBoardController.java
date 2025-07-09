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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class CommunityBoardController {

    private final CommunityBoardService communityBoardService;
    private final S3Service s3Service;

    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList("createdAt", "title", "likeCount", "viewCount");

    /**
     * 1. 게시글 목록 조회 (페이지네이션 + 정렬)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortBy 정렬 기준 필드
     * @param direction 정렬 방향 (ASC/DESC)
     * @param userPrincipal 로그인 사용자 정보
     * @return 게시글 목록
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<CommunityBoardResponse.SummaryDTO>>> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        // 잘못된 sortBy 값 방어
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<CommunityBoardResponse.SummaryDTO> boards = communityBoardService.getBoards(pageable, email);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(boards));
    }

    /**
     * 2. 게시글 상세 조회
     */
    @GetMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommunityBoardResponse.DetailDTO>> getBoardDetail(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        CommunityBoardResponse.DetailDTO board = communityBoardService.getBoardDetail(uuid, email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(board));
    }

    /**
     * 3. 게시글 작성
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommunityBoardResponse.DetailDTO>> createBoard(
            @RequestBody CommunityBoardRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        CommunityBoardResponse.DetailDTO board = communityBoardService.createBoard(request, email);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(board));
    }

    /**
     * 4. 게시글 수정
     */
    @PatchMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommunityBoardResponse.DetailDTO>> updateBoard(
            @PathVariable UUID uuid,
            @RequestBody CommunityBoardRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        CommunityBoardResponse.DetailDTO board = communityBoardService.updateBoard(uuid, request, email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(board));
    }

    /**
     * 5. 게시글 삭제
     */
    @DeleteMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String email = (userPrincipal != null) ? userPrincipal.getUsername() : null;

        communityBoardService.deleteBoard(uuid, email);

        return ResponseEntity.noContent().build();
    }
}
