package nova.mjs.comment.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class LikeCommentController {

    private final LikeCommentService likeCommentService;

    // 좋아요 추가 및 삭제 (토글 방식)
//    @PreAuthorize(isAuthrization() && userOr')
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.email.equals(principal.username)) or hasRole('ADMIN'))")

    @PostMapping("/{boardUUID}/comments/{commentUUID}/like")
    public ResponseEntity<ApiResponse<String>> toggleLike(@PathVariable UUID boardUUID, @PathVariable UUID commentUUID,
                                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isLiked = likeCommentService.toggleLike(boardUUID, commentUUID, userPrincipal.getUsername());
        String message = isLiked ? "좋아요가 추가되었습니다." : "좋아요가 취소되었습니다.";
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(message));
    }
}
