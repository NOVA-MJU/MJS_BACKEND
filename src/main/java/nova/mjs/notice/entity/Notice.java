package nova.mjs.notice.entity;

import com.amazonaws.services.ec2.model.Purchase;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "notice")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // 공지 제목
    private String date;        // 공지 날짜
    private String category;    // 공지 카테고리
    private String link;        // 공지 링크

    public static Notice createNotice(String title, String dateText, String type, String link) {

        return Notice.builder()
                .title(title)
                .date(dateText)
                .category(type)
                .link(link)
                .build();
    }

}

