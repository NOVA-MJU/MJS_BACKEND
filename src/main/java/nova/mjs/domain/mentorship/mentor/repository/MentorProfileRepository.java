package nova.mjs.domain.mentorship.mentor.repository;

import nova.mjs.domain.mentorship.mentor.entity.MentorProfile;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MentorProfile Repository
 *
 * MentorProfile은 Member에 1:1로 귀속되는 엔티티로,
 * "이 사용자가 멘토로 활동할 수 있는가?"를 판단하는 핵심 도메인이다.
 *
 * - MentorProfile은 로그인 주체가 아니다.
 * - 인증/권한은 Member(Role)에서 처리한다.
 * - 이 Repository는 멘토 자격/프로필 조회 용도로만 사용한다.
 */
@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {

    /* ===============================
       Member 기준 기본 조회
       =============================== */

    Optional<MentorProfile> findByMember(Member member);

    boolean existsByMemberId(Long memberId);

    Optional<MentorProfile> findByMemberAndActive(Member member, boolean active);

    /* ===============================
       Member.email 기반 조회 (API 계약용)
       =============================== */

    Optional<MentorProfile> findByMember_Email(String email);

    List<MentorProfile> findByMember_EmailIn(List<String> emails);
}
