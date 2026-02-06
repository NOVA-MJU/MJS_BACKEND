package nova.mjs.domain.thingo.broadcast.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nova.mjs.domain.thingo.broadcast.DTO.BroadcastResponseDTO;
import nova.mjs.domain.thingo.broadcast.entity.Broadcast;
import nova.mjs.domain.thingo.broadcast.exception.BroadcastSyncException;
import nova.mjs.domain.thingo.broadcast.repository.BroadcastRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class BroadcastService {
    private final BroadcastRepository broadcastRepository;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.channel.id}")
    private String channelId;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient youtubeClient;

    public BroadcastService(
            BroadcastRepository broadcastRepository,
            @Value("${youtube.api.key}") String apiKey,
            @Value("${youtube.channel.id}") String channelId,
            @Qualifier("youtubeApiClient") WebClient youtubeClient
    ) {
        this.broadcastRepository = broadcastRepository;
        this.apiKey = apiKey;
        this.channelId = channelId;
        this.youtubeClient = youtubeClient;
    }

    @Transactional
    public void syncAllByChannelId() {
        final LocalDateTime syncTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final LocalDateTime cutoff = syncTime.minusYears(3);

        try {
            syncAllPlaylists(syncTime, cutoff);
            syncUploadedVideos(syncTime, cutoff);

            broadcastRepository.deleteByPublishedAtBefore(cutoff);
            broadcastRepository.deleteByLastSyncedAtBefore(syncTime);

        } catch (Exception e) {
            throw new BroadcastSyncException();
        }
    }

    private void syncAllPlaylists(LocalDateTime syncTime, LocalDateTime cutoff) throws Exception {
        String pageToken = "";

        while (pageToken != null) {
            StringBuilder uriBuilder = new StringBuilder("/playlists")
                    .append("?part=snippet")
                    .append("&maxResults=50")
                    .append("&channelId=").append(channelId)
                    .append("&key=").append(apiKey);

            if (!pageToken.isEmpty()) {
                uriBuilder.append("&pageToken=").append(pageToken);
            }

            String json = youtubeClient.get()
                    .uri(uriBuilder.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(json);
            pageToken = root.path("nextPageToken").asText(null);

            for (JsonNode item : root.path("items")) {
                String playlistId = item.path("id").asText(null);
                if (playlistId == null || playlistId.isBlank()) continue;

                String playlistTitle = item.path("snippet").path("title").asText(null);
                syncPlaylistItems(playlistId, playlistTitle, syncTime, cutoff);
            }
        }
    }

    private void syncPlaylistItems(
            String playlistId,
            String playlistTitle,
            LocalDateTime syncTime,
            LocalDateTime cutoff
    ) throws Exception {

        String pageToken = "";

        while (pageToken != null) {
            StringBuilder uriBuilder = new StringBuilder("/playlistItems")
                    .append("?part=snippet,contentDetails")
                    .append("&maxResults=50")
                    .append("&playlistId=").append(playlistId)
                    .append("&key=").append(apiKey);

            if (!pageToken.isEmpty()) {
                uriBuilder.append("&pageToken=").append(pageToken);
            }

            String json = youtubeClient.get()
                    .uri(uriBuilder.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(json);
            pageToken = root.path("nextPageToken").asText(null);

            for (JsonNode item : root.path("items")) {
                JsonNode snippet = item.path("snippet");
                JsonNode contentDetails = item.path("contentDetails");

                String videoId = snippet.path("resourceId").path("videoId").asText(null);
                if (videoId == null || videoId.isBlank()) continue;

                String title = snippet.path("title").asText("");
                String thumbnail = snippet.path("thumbnails").path("high").path("url").asText("");

                String publishedAtStr = contentDetails.path("videoPublishedAt").asText(null);
                if (publishedAtStr == null || publishedAtStr.isBlank()) {
                    publishedAtStr = snippet.path("publishedAt").asText(null);
                }
                if (publishedAtStr == null || publishedAtStr.isBlank()) continue;

                LocalDateTime publishedAt = parseYoutubeDateTime(publishedAtStr);

                if (publishedAt.isBefore(cutoff)) continue;

                upsertBroadcast(videoId, title, thumbnail, publishedAt, playlistTitle, syncTime);
            }
        }
    }

    private void syncUploadedVideos(LocalDateTime syncTime, LocalDateTime cutoff) throws Exception {
        String url = "/channels?part=contentDetails&id=" + channelId + "&key=" + apiKey;

        String json = youtubeClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String uploadsId = objectMapper.readTree(json)
                .path("items").path(0)
                .path("contentDetails")
                .path("relatedPlaylists")
                .path("uploads")
                .asText(null);

        if (uploadsId == null || uploadsId.isBlank()) return;

        syncPlaylistItems(uploadsId, null, syncTime, cutoff);
    }

    private void upsertBroadcast(
            String videoId,
            String title,
            String thumbnailUrl,
            LocalDateTime publishedAt,
            String playlistTitle,
            LocalDateTime syncTime
    ) {
        broadcastRepository.findByVideoId(videoId).ifPresentOrElse(existing -> existing.syncFromYoutube(title, thumbnailUrl, publishedAt, playlistTitle, syncTime), () -> {
            Broadcast created = Broadcast.builder()
                    .videoId(videoId)
                    .title(title)
                    .url("https://www.youtube.com/watch?v=" + videoId)
                    .thumbnailUrl(thumbnailUrl)
                    .publishedAt(publishedAt)
                    .playlistTitle(playlistTitle) // null 가능
                    .lastSyncedAt(syncTime)
                    .build();
            broadcastRepository.save(created);
        });
    }

    private LocalDateTime parseYoutubeDateTime(String iso) {
        return OffsetDateTime.parse(iso).toLocalDateTime();
    }

    public Page<BroadcastResponseDTO> getBroadcasts(Pageable pageable) {
        return broadcastRepository.findAllByOrderByPublishedAtDesc(pageable)
                .map(b -> BroadcastResponseDTO.builder()
                        .title(b.getTitle())
                        .url(b.getUrl())
                        .thumbnailUrl(b.getThumbnailUrl())
                        .playlistTitle(b.getPlaylistTitle())
                        .publishedAt(b.getPublishedAt())
                        .build());
    }
}
