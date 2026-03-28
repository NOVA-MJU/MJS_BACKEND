package nova.mjs.domain.mentorship.communication.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.dto.ChatMessageDTO;
import nova.mjs.domain.mentorship.communication.service.ChatMessageServiceImpl;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat/details")
@RequiredArgsConstructor
public class ChatMessageQueryController {

    private final ChatMessageServiceImpl chatMessageService;

    @GetMapping("/{chatUuid}/messages")
    public ResponseEntity<ApiResponse<ChatMessageDTO.HistoryListResponse>> getMessages(
            @PathVariable UUID chatUuid,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID authenticatedUserUuid = userPrincipal.getUuid();

        ChatMessageDTO.HistoryListResponse response =
                chatMessageService.getMessages(chatUuid, authenticatedUserUuid);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}