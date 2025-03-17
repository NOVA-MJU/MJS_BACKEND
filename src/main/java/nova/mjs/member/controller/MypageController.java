package nova.mjs.member.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.member.service.MypageService;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    // 1. 내가 작성한 글 조회
    @GetMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CommunityBoardResponse>>> getMyPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CommunityBoardResponse> response = mypageService.getMyPosts(userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2. 내가 작성한 댓글이 속한 게시물 조회
    @GetMapping("/commented_posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CommunityBoardResponse>>> getMyCommentedPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CommunityBoardResponse> response = mypageService.getMyCommentedPosts(userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. 내가 찜한 글 조회
    @GetMapping("/liked_posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CommunityBoardResponse>>> getLikedPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CommunityBoardResponse> response = mypageService.getLikedPosts(userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
