package nova.mjs.domain.thingo.broadcast.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.broadcast.DTO.BroadcastResponseDTO;
import nova.mjs.domain.thingo.broadcast.service.BroadcastService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/broadcast")
public class BroadcastController {

    private final BroadcastService broadcastService;

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncAll() {
        broadcastService.syncAllByChannelId();
        return ResponseEntity.ok(ApiResponse.success("명지대학교 방송국 전체 영상 동기화 완료"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BroadcastResponseDTO>>> getBroadcasts(
            @PageableDefault(size = 9, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BroadcastResponseDTO> result = broadcastService.getBroadcasts(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

