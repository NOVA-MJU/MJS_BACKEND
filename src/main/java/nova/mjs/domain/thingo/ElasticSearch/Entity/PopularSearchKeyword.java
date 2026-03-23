package nova.mjs.domain.thingo.ElasticSearch.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "popular_search_keyword")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularSearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "popular_search_keyword_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeasonalPeriod period;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Column(nullable = false)
    private Integer displayOrder;

    public enum SeasonalPeriod {
        ENROLLMENT, // 개강: 3월, 9월
        EXAM,       // 시험: 4월, 10월
        SEMESTER_END, // 종강: 6월, 12월
        VACATION    // 방학: 1월, 2월, 7월, 8월
    }
}
