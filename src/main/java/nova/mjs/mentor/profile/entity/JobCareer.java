package nova.mjs.mentor.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.entity.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentor_job_career", indexes = {
        @Index(name = "idx_jobcareer_mentor_id", columnList = "mentor_id"),
        @Index(name = "idx_jobcareer_period", columnList = "started_at,ended_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class JobCareer extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 부모: Mentor */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    /** 회사명 */
    @Column(nullable = false, length = 128)
    private String company;

    /** 직무/포지션 */
    @Column(name = "job_position", nullable = false, length = 128)
    private String jobPosition;

    /** 기간 */
    @Column(name = "started_at")
    private LocalDate startedAt;

    @Column(name = "ended_at")
    private LocalDate endedAt; // null=재직중

    /** 수행 업무 요약 */
    @Column(name = "description", length = 500)
    private String description;

}
