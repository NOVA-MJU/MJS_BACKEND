package nova.mjs.domain.member.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.community.DTO.CommunityBoardResponse;
import nova.mjs.domain.member.DTO.CommentWithBoardResponse;
import nova.mjs.domain.member.DTO.ProfileCountResponse;
import nova.mjs.domain.member.service.ProfileService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 1. 내가 작성한 글 조회
    @GetMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CommunityBoardResponse.SummaryDTO>>> getMyPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CommunityBoardResponse.SummaryDTO> response = profileService.getMyPosts(userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2. 내가 작성한 댓글 조회
    @GetMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CommentWithBoardResponse>>> getMyCommentsWithBoard(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CommentWithBoardResponse> response = profileService.getMyCommentListWithBoard(userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. 내가 찜한 글 조회
    @GetMapping("/liked_posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CommunityBoardResponse.SummaryDTO>>> getLikedPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CommunityBoardResponse.SummaryDTO> response = profileService.getLikedPosts(userPrincipal.getUsername());
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
