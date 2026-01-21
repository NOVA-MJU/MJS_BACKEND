package nova.mjs.domain.thingo.news.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.domain.thingo.news.entity.News;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponseDTO {

    private String title;
    private LocalDateTime date;
    private String reporter;
    private String imageUrl;
    private String summary;
    private String link;
    private News.Category category;

    public static NewsResponseDTO fromEntity(News news) {
        return NewsResponseDTO.builder()
                .title(news.getTitle())
                .date(news.getDate())
                .reporter(news.getReporter())
                .imageUrl(news.getImageUrl())
                .summary(news.getSummary())
                .link(news.getLink())
                .category(news.getCategory())
                .build();
    }

    //엔티티 리스트를 DTO 리스트로 변환
    public static List<NewsResponseDTO> fromEntityToList(List<News> news) {
        return news.stream()
                .map(NewsResponseDTO::fromEntity)
                .toList();
    }
}
