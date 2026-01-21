package nova.mjs.domain.thingo.member.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.thingo.member.DTO.CommentWithBoardResponse;
import nova.mjs.domain.thingo.member.DTO.ProfileCountResponse;
import nova.mjs.domain.thingo.member.service.ProfileService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

// 1. 내가 작성한 글 조회 (페이지네이션)
    @GetMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<CommunityBoardResponse.SummaryDTO>>> getMyPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size // 한 페이지에 5개씩
    ) {
        Page<CommunityBoardResponse.SummaryDTO> response =
                profileService.getMyPosts(userPrincipal.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

// 2. 내가 작성한 댓글 조회 (페이지네이션)
    @GetMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<CommentWithBoardResponse>>> getMyCommentsWithBoard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<CommentWithBoardResponse> response =
                profileService.getMyCommentListWithBoard(userPrincipal.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


// 3. 내가 찜한 글 조회 (페이지네이션)
    @GetMapping("/liked_posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<CommunityBoardResponse.SummaryDTO>>> getLikedPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<CommunityBoardResponse.SummaryDTO> response =
                profileService.getLikedPosts(userPrincipal.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

// 4. 작성글, 댓글, 찜 갯수
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileCountResponse>> getMyProfileSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ProfileCountResponse response = profileService.getMyProfileSummary(userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}