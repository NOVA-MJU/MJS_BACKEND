package nova.mjs.domain.thingo.notice.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.thingo.ElasticSearch.EntityListner.NoticeEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(NoticeEntityListener.class)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;       // 공지 제목

    @Column(columnDefinition = "TEXT")
    private String content;   // 공지 내용

    @Column(nullable = false)
    private LocalDateTime date; // 공지 날짜

    @Column(nullable = false)
    private String category;    // 공지 카테고리

    @Column(nullable = false, length = 1000)
    private String link;        // 공지 링크

    @Column(nullable = false)
    private Integer viewCount;  // 공지 실제 조회수(원본 사이트)

    @Column(nullable = false)
    private Integer viewCountDeltaToday; // 오늘 누적 조회수 증가량(급상승 계산용)

    @Column(nullable = false)
    private LocalDate viewCountDeltaDate; // 누적 기준일

    public static Notice createNotice(String title, String content, LocalDateTime date, String type, String link, int viewCount) {
        LocalDate today = LocalDate.now();
        return Notice.builder()
                .title(title)
                .content(content)
                .date(date)
                .category(type)
                .link(link)
                .viewCount(Math.max(0, viewCount))
                .viewCountDeltaToday(0)
                .viewCountDeltaDate(today)
                .build();
    }

    /**
     * 크롤링한 조회수를 반영한다.
     * - 날짜가 바뀌면 증가량 카운터를 리셋한다.
     * - 역전(원본 사이트 보정) 시 음수 증가량은 누적하지 않는다.
     */
    public void applyCrawledViewCount(int crawledViewCount, LocalDate crawledDate) {
        int normalizedCount = Math.max(0, crawledViewCount);

        if (viewCountDeltaDate == null || !viewCountDeltaDate.equals(crawledDate)) {
            viewCountDeltaDate = crawledDate;
            viewCountDeltaToday = 0;
        }

        int diff = normalizedCount - Math.max(0, viewCount == null ? 0 : viewCount);
        if (diff > 0) {
            viewCountDeltaToday += diff;
        }

        viewCount = normalizedCount;
    }
}
