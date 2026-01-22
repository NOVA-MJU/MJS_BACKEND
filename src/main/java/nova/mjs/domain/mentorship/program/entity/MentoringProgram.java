package nova.mjs.domain.mentorship.program.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.mentorship.mentor.entity.MentorProfile;
import nova.mjs.util.entity.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "mentoring_program",
        indexes = {
                @Index(name = "idx_mentoring_program_uuid", columnList = "uuid", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class MentoringProgram extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 외부 노출용 UUID */
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    /** 프로그램 제목 */
    @Column(nullable = false)
    private String title;

    /** 시작일 */
    @Column(nullable = false)
    private LocalDate startDate;

    /** 종료일 */
    @Column(nullable = false)
    private LocalDate endDate;

    /** 모집 인원 */
    @Column(nullable = false)
    private int capacity;

    /** 대상 */
    @Column(nullable = false)
    private String targetAudience;

    /** 진행 장소 */
    @Column(nullable = false)
    private String location;

    /** 문의처 */
    @Column(nullable = false)
    private String contact;

    /** 프로그램 설명 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    /** 준비사항 및 주의사항 */
    @Column(columnDefinition = "TEXT")
    private String preparation;

    /** 참여 멘토 */
    @ManyToMany
    @JoinTable(
            name = "mentoring_program_mentor",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "mentor_profile_id")
    )
    private List<MentorProfile> mentors = new ArrayList<>();

    // =========================
    // 생성 팩토리
    // =========================
    public static MentoringProgram create(
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            int capacity,
            String targetAudience,
            String location,
            String contact,
            String preparation,
            List<MentorProfile> mentors
    ) {
        return MentoringProgram.builder()
                .uuid(UUID.randomUUID())
                .title(title)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .capacity(capacity)
                .targetAudience(targetAudience)
                .location(location)
                .contact(contact)
                .preparation(preparation)
                .mentors(mentors)
                .build();
    }
}
