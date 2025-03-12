package nova.mjs.news.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.member.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "MJU_News")
public class News {
    @Id
    @Column(name = "news_id")
    private Long id; //기사 id

    @Column(nullable = false)
    private String title; //기사 제목

    @Column(nullable = false)
    private String date; //기사 날짜

    @Column(nullable = false)
    private String reporter; //기자 이름

    @Column(nullable = false)
    private String imageUrl; //이미지

    @Column(columnDefinition = "TEXT")
    private String summary; //기사 첫 문단

    @Column(nullable = false, unique = true)
    private String link; //기사 링크

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Category category; //기사 카테고리

    public enum Category {
        REPORT, SOCIETY;

        public static Category fromStringTOUppercase(String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Category cannot be null or blank");
            }
            try {
                return Category.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new IllegalArgumentException("Invalid category value: " + value);
            }
        }
    }

    public static News createNews(Long id, String title, String date, String reporter, String imageUrl, String summary, String link, String category) {
        return News.builder()
                .id(id)
                .title(title)
                .date(date)
                .reporter(reporter)
                .imageUrl(imageUrl)
                .summary(summary)
                .link(link)
                .category(Category.fromStringTOUppercase(category))
                .build();
    }
}

