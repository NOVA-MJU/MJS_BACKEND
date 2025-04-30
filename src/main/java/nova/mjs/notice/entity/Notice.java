package nova.mjs.notice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "notice")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(nullable = false)
    private String title;       // 공지 제목

    @Column(nullable = false)
    private LocalDate date;        // 공지 날짜

    @Column(nullable = false)
    private String category;    // 공지 카테고리

    @Column(nullable = false)
    private String link;        // 공지 링크

    public static Notice createNotice(String title, LocalDate date, String type, String link) {
        return Notice.builder()
                .title(title)
                .date(date)
                .category(type)
                .link(link)
                .build();
    }

}

