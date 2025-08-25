package nova.mjs.mentor.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentor_portfolio", indexes = {
        @Index(name = "idx_portfolio_mentor_id", columnList = "mentor_id"),
        @Index(name = "idx_portfolio_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Portfolio extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 부모: Mentor */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    /** 제목 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 요약/설명 (text) */
    @Column(columnDefinition = "text")
    private String description;

    /** 대표 링크(옵션) */
    @Column(length = 500)
    private String link;

    /** 기술스택 (문자열 리스트) */
    @ElementCollection
    @CollectionTable(name = "portfolio_skills", joinColumns = @JoinColumn(name = "portfolio_id"))
    @Column(name = "skill", nullable = false, length = 100)
    @Builder.Default
    private List<String> skills = new ArrayList<>();

}
