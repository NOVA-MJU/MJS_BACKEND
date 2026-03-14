package nova.mjs.domain.mentorship.communication.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.dto.ChatMessageDTO;
import nova.mjs.domain.mentorship.communication.service.ChatMessageServiceImpl;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat/details")
@RequiredArgsConstructor
public class ChatMessageQueryController {

    private final ChatMessageServiceImpl chatMessageService;

    @GetMapping("/{chatUuid}/messages")
    public ResponseEntity<ApiResponse<ChatMessageDTO.HistoryListResponse>> getMessages(
            @PathVariable UUID chatUuid
    ) {
        ChatMessageDTO.HistoryListResponse response = chatMessageService.getMessages(chatUuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}