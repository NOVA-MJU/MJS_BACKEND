package nova.mjs.domain.thingo.broadcast.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nova.mjs.domain.thingo.broadcast.DTO.BroadcastResponseDTO;
import nova.mjs.domain.thingo.broadcast.entity.Broadcast;
import nova.mjs.domain.thingo.broadcast.repository.BroadcastRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public void syncAllByChannelId() {
        Set<String> currentVideoIds = new HashSet<>();

        // 1. 재생목록 기반 영상 저장
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

            try {
                JsonNode root = objectMapper.readTree(json);
                pageToken = root.path("nextPageToken").asText(null);
                JsonNode items = root.get("items");

                for (JsonNode item : items) {
                    String playlistId = item.path("id").asText();
                    String playlistTitle = item.path("snippet").path("title").asText();

                    currentVideoIds.addAll(syncPlaylist(playlistId, playlistTitle));
                }

            } catch (Exception e) {
                throw new RuntimeException("재생목록 조회 실패", e);
            }
        }

        // 2. 업로드 전용 재생목록 → playlistTitle = null
        currentVideoIds.addAll(syncUploadedVideos());

        // 3. DB 정리 (삭제)
        List<Broadcast> all = broadcastRepository.findAll();
        for (Broadcast video : all) {
            if (!currentVideoIds.contains(video.getVideoId())) {
                broadcastRepository.delete(video);
            }
        }
    }

    private Set<String> syncPlaylist(String playlistId, String playlistTitle) {
        Set<String> videoIds = new HashSet<>();
        String pageToken = "";

        while (pageToken != null) {
            StringBuilder uriBuilder = new StringBuilder("/playlistItems")
                    .append("?part=snippet")
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

            try {
                JsonNode root = objectMapper.readTree(json);
                pageToken = root.path("nextPageToken").asText(null);
                JsonNode items = root.get("items");

                for (JsonNode item : items) {
                    JsonNode snippet = item.get("snippet");
                    String videoId = snippet.path("resourceId").path("videoId").asText();
                    String title = snippet.path("title").asText();
                    String thumbnail = snippet.path("thumbnails").path("high").path("url").asText();
                    String publishedAtStr = snippet.path("publishedAt").asText();
                    LocalDateTime publishedAt = LocalDateTime.parse(publishedAtStr.replace("Z", ""));

                    if (publishedAt.isBefore(LocalDateTime.now().minusYears(4))) continue;

                    videoIds.add(videoId);

                    broadcastRepository.findByVideoId(videoId).orElseGet(() ->
                            broadcastRepository.save(
                                    Broadcast.builder()
                                            .videoId(videoId)
                                            .title(title)
                                            .url("https://www.youtube.com/watch?v=" + videoId)
                                            .thumbnailUrl(thumbnail)
                                            .publishedAt(publishedAt)
                                            .playlistTitle(playlistTitle)
                                            .build()
                            )
                    );
                }

            } catch (Exception e) {
                throw new RuntimeException("영상 항목 조회 실패", e);
            }
        }

        return videoIds;
    }

    private Set<String> syncUploadedVideos() {
        Set<String> videoIds = new HashSet<>();

        String url = "/channels?part=contentDetails&id=" + channelId + "&key=" + apiKey;

        String json = youtubeClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            String uploadsId = objectMapper.readTree(json)
                    .path("items").get(0)
                    .path("contentDetails")
                    .path("relatedPlaylists")
                    .path("uploads")
                    .asText();

            videoIds.addAll(syncPlaylist(uploadsId, null)); // playlistTitle = null

        } catch (Exception e) {
            throw new RuntimeException("업로드 영상 조회 실패", e);
        }

        return videoIds;
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