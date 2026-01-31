package nova.mjs.domain.mentorship.application.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.mentorship.program.entity.MentoringProgram;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.util.entity.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "mentorship_application",
        indexes = {
                @Index(name = "idx_application_uuid", columnList = "uuid", unique = true)
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorshipApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 외부 노출용 UUID */
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    /** 신청자 */
    @ManyToOne(fetch = FetchType.LAZY)
    private Member applicant;

    /** 대상 멘토 */
    @ManyToOne(fetch = FetchType.LAZY)
    private Member mentor;

    /** 신청자 정보 스냅샷 */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String studentNumber;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    /** 졸업 예정 */
    private LocalDate expectedGraduation;

    /** 멘토링 주제 */
    @Column(columnDefinition = "TEXT")
    private String topic;

    /** 개인정보 이용 동의 */
    @Column(nullable = false)
    private boolean privacyAgreement;

    /** 신청 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private MentoringProgram program;


    /* =========================
       내부 상태 enum
       ========================= */
    public enum ApplicationStatus {
        SUBMITTED,   // 신청 완료
        ACCEPTED,    // 수락
        REJECTED     // 거절
    }

    /* =========================
       생성 팩토리 (Builder 사용)
       ========================= */
    public static MentorshipApplication create(
            Member applicant,
            Member mentor,
            String name,
            String studentNumber,
            String department,
            String grade,
            String phone,
            String email,
            LocalDate expectedGraduation,
            String topic,
            boolean privacyAgreement
    ) {
        return MentorshipApplication.builder()
                .uuid(UUID.randomUUID())
                .applicant(applicant)
                .mentor(mentor)
                .name(name)
                .studentNumber(studentNumber)
                .department(department)
                .grade(grade)
                .phone(phone)
                .email(email)
                .expectedGraduation(expectedGraduation)
                .topic(topic)
                .privacyAgreement(privacyAgreement)
                .status(ApplicationStatus.SUBMITTED)
                .build();
    }

    /* =========================
       상태 전이
       ========================= */

    public void accept() {
        if (this.status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("수락할 수 없는 신청 상태입니다.");
        }
        this.status = ApplicationStatus.ACCEPTED;
    }

    public void reject() {
        if (this.status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("거절할 수 없는 신청 상태입니다.");
        }
        this.status = ApplicationStatus.REJECTED;
    }
}
