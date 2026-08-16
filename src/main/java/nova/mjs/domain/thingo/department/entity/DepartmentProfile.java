package nova.mjs.domain.thingo.department.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.util.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "department_profiles")
public class DepartmentProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false, unique = true)
    private Department department;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String educationalGoals;

    @Column(columnDefinition = "TEXT")
    private String careerPaths;

    @Column(columnDefinition = "TEXT")
    private String majorCurriculum;

    @Column(length = 1000)
    private String introductionSourceUrl;

    @Column(length = 1000)
    private String educationalGoalsSourceUrl;

    @Column(length = 1000)
    private String careerPathsSourceUrl;

    @Column(length = 1000)
    private String majorCurriculumSourceUrl;

    @Column(nullable = false, length = 30)
    private String collectionStatus;

    @Column(columnDefinition = "TEXT")
    private String collectionMessage;

    private LocalDateTime verifiedAt;

    public static DepartmentProfile create(Department department) {
        return DepartmentProfile.builder()
                .department(department)
                .collectionStatus("PENDING")
                .build();
    }

    public void updateOfficialContent(
            String introduction,
            String educationalGoals,
            String careerPaths,
            String majorCurriculum,
            String introductionSourceUrl,
            String educationalGoalsSourceUrl,
            String careerPathsSourceUrl,
            String majorCurriculumSourceUrl,
            String collectionStatus,
            String collectionMessage,
            LocalDateTime verifiedAt
    ) {
        this.introduction = introduction;
        this.educationalGoals = educationalGoals;
        this.careerPaths = careerPaths;
        this.majorCurriculum = majorCurriculum;
        this.introductionSourceUrl = introductionSourceUrl;
        this.educationalGoalsSourceUrl = educationalGoalsSourceUrl;
        this.careerPathsSourceUrl = careerPathsSourceUrl;
        this.majorCurriculumSourceUrl = majorCurriculumSourceUrl;
        this.collectionStatus = collectionStatus;
        this.collectionMessage = collectionMessage;
        this.verifiedAt = verifiedAt;
    }
}
