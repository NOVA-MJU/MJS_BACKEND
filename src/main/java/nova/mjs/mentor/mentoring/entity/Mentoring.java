package nova.mjs.mentor.mentoring.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.mentor.profile.entity.Mentor;
import nova.mjs.util.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "mentoring",
        indexes = {
                @Index(name = "uk_mentoring_uuid", columnList = "uuid", unique = true),
                @Index(name = "idx_mentoring_mentor", columnList = "mentor_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Mentoring extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 비즈니스 키 */
    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID uuid;

    /** 멘토링 제목 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 멘토 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mentoring_mentor"))
    private Mentor mentor;

    /** 멘토링 설명 (PostgreSQL text) */
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    /** 멘토링 주제 (기본 타입 컬렉션) */
    @ElementCollection
    @CollectionTable(name = "mentoring_subjects", joinColumns = @JoinColumn(name = "mentoring_id"))
    @Column(name = "subject", nullable = false, length = 100)
    @Builder.Default
    private List<String> subjects = new ArrayList<>();

    /** 최대 멘티 수 */
    @Column(name = "max_mentee")
    private int maxMentee;

    /** 가능한 시간대 (예: 문자열 포맷/JSON 등) */
    @Column(name = "available_time", nullable = false, length = 200)
    private String availableTime;

    /** 감사 메시지 (자식 엔티티) */
    @OneToMany(mappedBy = "mentoring", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ThanksMessage> thanksMessages = new ArrayList<>();

}
