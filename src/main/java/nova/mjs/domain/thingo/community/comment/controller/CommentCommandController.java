package nova.mjs.domain.thingo.community.comment.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.community.comment.service.CommentService;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentCommandController {

    private final CommentService commentService;

    @DeleteMapping("/{commentUUID}")
    @PreAuthorize("isAuthenticated() and ((#userPrincipal.email.equals(principal.username)) or hasRole('ADMIN'))")
    public ResponseEntity<Void> deleteCommentByCommentUuid(
            @PathVariable UUID commentUUID,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        commentService.deleteCommentByUuid(commentUUID, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
