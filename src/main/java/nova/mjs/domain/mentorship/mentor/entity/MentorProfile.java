package nova.mjs.domain.mentorship.mentor.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.thingo.member.entity.Member;

@Entity
@Table(name = "mentor_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 로그인 / 인증 주체
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    /**
     * 멘토링 화면 노출 이름
     */
    @Column(nullable = false)
    private String displayName;

    /**
     * 멘토 프로필 이미지
     */
    private String profileImageUrl;

    /**
     * 소속 학과 / 전문 분야
     */
    @Column(nullable = false)
    private String departmentName;

    /**
     * 멘토 소개
     */
    @Column(columnDefinition = "TEXT")
    private String introduction;

    /**
     * 멘토 활동 가능 여부
     */
    @Column(nullable = false)
    private boolean active;

    // ===========================
    // 생성 팩토리 메서드
    // ===========================

    /**
     * 멘토 신청 / 승인 시 MentorProfile 생성
     */
    public static MentorProfile create(
            Member member,
            String displayName,
            String departmentName,
            String introduction,
            String profileImageUrl
    ) {
        return MentorProfile.builder()
                .member(member)
                .displayName(displayName)
                .departmentName(departmentName)
                .introduction(introduction)
                .profileImageUrl(profileImageUrl)
                .active(true) // 최초 생성 시 활성화
                .build();
    }

    // ===========================
    // 수정 메서드
    // ===========================

    /**
     * 멘토 프로필 수정
     * null 값은 기존 값 유지
     */
    public void update(
            String displayName,
            String departmentName,
            String introduction,
            String profileImageUrl
    ) {
        this.displayName = getOrDefault(displayName, this.displayName);
        this.departmentName = getOrDefault(departmentName, this.departmentName);
        this.introduction = getOrDefault(introduction, this.introduction);
        this.profileImageUrl = getOrDefault(profileImageUrl, this.profileImageUrl);
    }

    // ===========================
    // 상태 변경
    // ===========================

    /**
     * 멘토 활동 비활성화
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * 멘토 활동 재활성화
     */
    public void activate() {
        this.active = true;
    }

    // ===========================
    // 내부 유틸
    // ===========================

    private <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }
}
