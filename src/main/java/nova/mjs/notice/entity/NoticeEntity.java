package nova.mjs.notice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // 공지 제목
    private String date;        // 공지 날짜
    private String category;    // 공지 카테고리
    private String link;        // 공지 링크
}
