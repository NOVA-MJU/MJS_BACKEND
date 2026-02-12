package nova.mjs.domain.thingo.department.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_council_notice_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudentCouncilNoticeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private StudentCouncilNotice notice;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int sequence;

    public static StudentCouncilNoticeImage of(
            StudentCouncilNotice notice,
            String url,
            int sequence
    ) {
        return StudentCouncilNoticeImage.builder()
                .notice(notice)
                .imageUrl(url)
                .sequence(sequence)
                .build();
    }
}
