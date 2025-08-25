package nova.mjs.mentor.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.mentor.mentoring.entity.Mentoring;
import nova.mjs.mentor.profile.dto.MentorProfileDTO;
import nova.mjs.util.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "mentor",
    indexes = { @Index(name = "uk_mentor_member", columnList = "member_id", unique = true)}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Mentor extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_mentor_member"))
    private Member member;


    /** 연락처(멘토 프로필 전용) */
    @Column(nullable = false, length = 32)
    private String phoneNumber;

    /** 졸업연도 */
    @Column(nullable = false)
    private int graduationYear;

    /** 총 경력 연차 */
    @Column(name = "career_year", nullable = false)
    private int careerYear;

    /** 현재 직장/회사 */
    @Column(name = "workplace", length = 128)
    private String workplace;

    /** 현재 직무/포지션 */
    @Column(name = "job_title", length = 128)
    private String jobTitle;

    /** 프로필 소개 (PostgreSQL text) */
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    /** 사실 여부 검증 */
    @Column(nullable = false)
    private boolean isVerified;

    /** 추가 노하우 (text) */
    @Column(name = "resume_tips",    columnDefinition = "text")
    private String resumeTips;
    @Column(name = "interview_tips", columnDefinition = "text")
    private String interviewTips;
    @Column(name = "portfolio_tips", columnDefinition = "text")
    private String portfolioTips;
    @Column(name = "networking_tips", columnDefinition = "text")
    private String networkingTips;

    /** 커리어 이력 (자식 엔티티) */
    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobCareer> careers = new ArrayList<>();

    /** 포트폴리오 (자식 엔티티) */
    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Portfolio> portfolios = new ArrayList<>();

    /** 보유 스킬 (단순 문자열 리스트) */
    @ElementCollection
    @CollectionTable(name = "mentor_skills", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "skill", nullable = false, length = 100)
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    /** 멘토링(외부 도메인) */
    @OneToMany(mappedBy = "mentor", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Mentoring> mentorings = new ArrayList<>();

    /* ===== 정적 팩토리 / 부분 업데이트 (DTO 기반, null-safe) ===== */

    // DTO는 별도 패키지에서 정의 예정
    public static Mentor create(Member member, MentorProfileDTO.Request request) {
        return Mentor.builder()
                .member(member)
                .phoneNumber(digitsOnly(request.getPhoneNumber()))
                .graduationYear(request.getGraduationYear())
                .careerYear(request.getCareerYear())
                .workplace(request.getWorkplace())
                .jobTitle(request.getJobTitle())
                .description(request.getDescription())
                .isVerified(request.getIsVerified() != null ? request.getIsVerified() : false)
                .resumeTips(request.getResumeTips())
                .interviewTips(request.getInterviewTips())
                .portfolioTips(request.getPortfolioTips())
                .networkingTips(request.getNetworkingTips())
                .build();
    }

    /** dto의 각 필드가 null이면 기존 값 유지 */
    public Mentor update(nova.mjs.mentor.profile.dto.MentorProfileDTO.Update dto) {
        this.phoneNumber    = getOrDefault(digitsOnly(dto.getPhoneNumber()), this.phoneNumber);
        this.graduationYear = getOrDefault(dto.getGraduationYear(), this.graduationYear);
        this.careerYear     = getOrDefault(dto.getCareerYear(), this.careerYear);
        this.workplace      = getOrDefault(dto.getWorkplace(), this.workplace);
        this.jobTitle       = getOrDefault(dto.getJobTitle(), this.jobTitle);
        this.description    = getOrDefault(dto.getDescription(), this.description);
        this.isVerified     = getOrDefault(dto.getIsVerified(), this.isVerified);
        this.resumeTips     = getOrDefault(dto.getResumeTips(), this.resumeTips);
        this.interviewTips  = getOrDefault(dto.getInterviewTips(), this.interviewTips);
        this.portfolioTips  = getOrDefault(dto.getPortfolioTips(), this.portfolioTips);
        this.networkingTips = getOrDefault(dto.getNetworkingTips(), this.networkingTips);
        // skills, careers, portfolios는 별도 명령 API로 교체/추가/삭제를 권장
        return this;
    }

    private <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }

    /** 전화번호 하이픈/공백/괄호/플러스 등 제거 → 숫자만 남김 */
    private static String digitsOnly(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("\\D+", ""); // \D = 숫자가 아닌 모든 문자
        return digits.isEmpty() ? null : digits;    // 전부 비었으면 null 처리(선택)
    }


}
