package nova.mjs.domain.broadcast.DTO;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BroadcastResponseDTO {
    private String title;
    private String url;
    private String thumbnailUrl;
    private String playlistTitle;
    private LocalDateTime publishedAt;
}