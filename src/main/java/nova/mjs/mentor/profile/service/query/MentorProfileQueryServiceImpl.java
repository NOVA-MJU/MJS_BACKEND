package nova.mjs.mentor.profile.service.query;

import lombok.RequiredArgsConstructor;
import nova.mjs.mentor.mentoring.repository.MentoringRepository;
import nova.mjs.mentor.mentoring.repository.ThanksMessageRepository;
import nova.mjs.mentor.profile.dto.*;
import nova.mjs.mentor.profile.entity.Mentor;
import nova.mjs.mentor.profile.repository.MentorRepository;
import nova.mjs.mentor.profile.repository.MentorSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
public class MentorProfileQueryServiceImpl implements MentorProfileQueryService {

    private final MentorRepository mentorRepository;
    private final MentoringRepository mentoringRepository;
    private final ThanksMessageRepository thanksMessageRepository;

    @Override
    public MentorStatsDTO getOverviewStatistics() {
        long mentorCount = mentorRepository.count();
        long jobCategoryCount = 0L; // 직무 카테고리 스키마 미도입
        long totalConsultations = mentoringRepository.countAllConsultations();
        Integer averageResponseRate = null; // 추후 도입
        return MentorStatsDTO.builder()
                .mentorCount(mentorCount)
                .jobCategoryCount(jobCategoryCount)
                .totalConsultations(totalConsultations)
                .averageResponseRate(averageResponseRate)
                .build();
    }

    @Override
    public Page<MentorListItemDTO> findMentorCards(MentorSearchConditionDTO condition, Pageable pageable) {
        Specification<Mentor> mentorFiltersSpec = where(MentorSpecs.keyword(condition.getQ()))
                .and(MentorSpecs.hasAnySkill(condition.getSkills()));
        Pageable pageableWithSort = resolvePageableWithSortOption(pageable, condition.getSort());
        return mentorRepository.findAll(mentorFiltersSpec, pageableWithSort)
                .map(mentor -> {
                    var member = mentor.getMember();
                    String maskedDisplayName = toMaskedDisplayName(member != null ? member.getName() : null) + " 선배";
                    String departmentName = member != null && member.getDepartmentName() != null ? member.getDepartmentName().name() : null;
                    String profileImageUrl = (member != null) ? member.getProfileImageUrl() : null; // 없으면 null
                    long mentoringCount = mentoringRepository.countByMentor_Id(mentor.getId());
                    return MentorListItemDTO.from(mentor, mentoringCount, maskedDisplayName, departmentName, profileImageUrl, false, null, null);
                });
    }

    @Override
    public List<MentorListItemDTO> findFeaturedMentorsForMonth(YearMonth month) {
        // featured 스키마 미도입 → 우선 빈 리스트(추후 컬럼/정책 추가 시 구현)
        return List.of();
    }

    @Override
    public MentorDetailDTO getMentorDetailByMemberUuid(UUID memberUuid) {
        Mentor mentor = mentorRepository.findWithDetailsByMember_Uuid(memberUuid)
                .orElseThrow(() -> new IllegalArgumentException("mentor not found by memberUuid"));
        var member = mentor.getMember();
        String displayName = toMaskedDisplayName(member != null ? member.getName() : null);
        String profileImageUrl = (member != null) ? member.getProfileImageUrl() : null;
        String departmentName = member != null && member.getDepartmentName() != null ? member.getDepartmentName().name() : null;
        String schoolName = member != null && member.getCollege() != null ? member.getCollege().name() : null;
        // UI에 대학 칸이 있길래 그냥 이렇게 넣었는데 사실 명지대 말고 들어가나?
        return MentorDetailDTO.from(mentor, displayName, profileImageUrl, departmentName, mentor.getGraduationYear(), schoolName);
    }

    @Override
    public MentorMetricsDTO getMentorMetricsByMemberUuid(UUID memberUuid, Long viewerIdNullable) {
        // memberUuid → mentor 조회 후 기존 카운트 메서드(mentorId 기반) 사용
        Mentor mentor = mentorRepository.findByMember_Uuid(memberUuid)
                .orElseThrow(() -> new IllegalArgumentException("mentor not found by memberUuid"));
        Long mentorId = mentor.getId();
        long viewCount = 0L; // 추후 도입
        long thanksMessageCount = thanksMessageRepository.countByMentoring_Mentor_Id(mentorId);
        long mentoringCount = mentoringRepository.countByMentor_Id(mentorId);
        return MentorMetricsDTO.builder()
                .viewCount(viewCount)
                .thanksCount(thanksMessageCount)
                .mentoringCount(mentoringCount)
                .bookmarked(null)
                .build();
    }

    private Pageable resolvePageableWithSortOption(Pageable pageable, String sort) {
        if (sort == null || sort.isBlank() || "recent".equals(sort)) return pageable;
        return pageable; // popular/responseRate 컬럼 도입 전
    }

    private static String toMaskedDisplayName(String name) {
        if (name == null || name.isBlank()) return "";
        return name.substring(0, 1) + "OO";
    }
}