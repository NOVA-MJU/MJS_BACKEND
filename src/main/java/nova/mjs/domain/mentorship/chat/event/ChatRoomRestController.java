package nova.mjs.domain.mentorship.chat.event;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.mentorship.chat.event.ChatRoomCreateRequestDto;
import nova.mjs.domain.mentorship.chat.event.ChatRoomCreatedEventDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatRoomRestController {

    private final ChatRoomCreateUseCase chatRoomCreateRestUseCase;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomCreatedEventDto> createRoom(
            @RequestBody ChatRoomCreateRequestDto req,
            Authentication authentication
    ) {
        ChatRoomCreatedEventDto result = chatRoomCreateRestUseCase.createRoom(req, authentication);
        return ResponseEntity.ok(result);
    }
}
