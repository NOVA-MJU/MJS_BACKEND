package nova.mjs.domain.thingo.broadcast.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "broadcast")
public class Broadcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String videoId;

    private String title;

    private String url;

    private String thumbnailUrl;

    private String playlistTitle; // 재생목록

    private LocalDateTime publishedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastSyncedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void syncFromYoutube(
            String title,
            String thumbnailUrl,
            LocalDateTime publishedAt,
            String playlistTitle,
            LocalDateTime syncTime
    ) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.publishedAt = publishedAt;

        if (playlistTitle != null && !playlistTitle.isBlank()) {
            this.playlistTitle = playlistTitle;
        }

        this.lastSyncedAt = syncTime;
    }
}
