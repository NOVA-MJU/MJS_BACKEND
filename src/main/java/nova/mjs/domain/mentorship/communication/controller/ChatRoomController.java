package nova.mjs.domain.mentorship.communication.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.communication.dto.ChatRoomDTO;
import nova.mjs.domain.mentorship.communication.service.ChatRoomServiceImpl;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat/room")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomServiceImpl chatRoomService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomDTO.CreateResponse>> createChatRoom(
            @RequestBody ChatRoomDTO.CreateRequest request
    ) {
        ChatRoomDTO.CreateResponse response = chatRoomService.createChatRoom(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @DeleteMapping("/{chatUuid}")
    public ResponseEntity<ApiResponse<ChatRoomDTO.DeleteResponse>> deleteChatRoom(
            @PathVariable UUID chatUuid
    ) {
        ChatRoomDTO.DeleteResponse response = chatRoomService.deleteChatRoom(chatUuid);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomDTO.SummaryResponse>>> getMyChatRooms(
            @RequestParam UUID memberUuid
    ) {
        List<ChatRoomDTO.SummaryResponse> response = chatRoomService.getMyChatRooms(memberUuid);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


}