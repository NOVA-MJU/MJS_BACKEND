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

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
public class MentorProfileQueryServiceImpl implements MentorProfileQueryService {

    private final MentorRepository mentorRepo;
    private final MentoringRepository mentoringRepo;
    private final ThanksMessageRepository thanksRepo;

    public MentorStatsDTO stats() {
        long mentorCount = mentorRepo.count();
        long jobCategoryCount = 0L; // 직무 카테고리 스키마 미도입
        long totalConsultations = mentoringRepo.countAllConsultations();
        Integer averageResponseRate = null; // 추후 도입
        return MentorStatsDTO.builder()
                .mentorCount(mentorCount)
                .jobCategoryCount(jobCategoryCount)
                .totalConsultations(totalConsultations)
                .averageResponseRate(averageResponseRate)
                .build();
    }

    public Page<MentorListItemDTO> search(MentorSearchConditionDTO cond, Pageable pageable) {
        Specification<Mentor> spec = where(MentorSpecs.keyword(cond.getQ()))
                .and(MentorSpecs.hasAnySkill(cond.getSkills()));
        Pageable sorted = applySort(pageable, cond.getSort());
        return mentorRepo.findAll(spec, sorted)
                .map(m -> {
                    var member = m.getMember();
                    String displayName = mask(member != null ? member.getName() : null) + " 선배";
                    String department = member != null && member.getDepartmentName() != null ? member.getDepartmentName().name() : null;
                    String profileUrl = (member != null) ? member.getProfileImageUrl() : null; // 없으면 null
                    long mentoringCount = mentoringRepo.countByMentor_Id(m.getId());
                    return MentorListItemDTO.from(m, mentoringCount, displayName, department, profileUrl, false, null, null);
                });
    }

    public List<MentorListItemDTO> featured(YearMonth month) {
        // featured 스키마 미도입 → 우선 빈 리스트(추후 컬럼/정책 추가 시 구현)
        return List.of();
    }

    public MentorDetailDTO getDetail(Long id) {
        Mentor m = mentorRepo.findWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("mentor not found"));
        var member = m.getMember();
        String displayName = mask(member != null ? member.getName() : null);
        String profileUrl = (member != null) ? member.getProfileImageUrl() : null;
        String department = member != null && member.getDepartmentName() != null ? member.getDepartmentName().name() : null;
        String school = member != null && member.getCollege() != null ? member.getCollege().name() : null;
        // UI에 대학 칸이 있길래 그냥 이렇게 넣었는데 사실 명지대 말고 들어가나?
        return MentorDetailDTO.from(m, displayName, profileUrl, department, m.getGraduationYear(), school);
    }

    public MentorMetricsDTO getMetrics(Long id, Long viewerIdNullable) {
        long viewCount = 0L; // 도입 전
        long thanks = thanksRepo.countByMentoring_Mentor_Id(id);
        long mentoringCount = mentoringRepo.countByMentor_Id(id);
        return MentorMetricsDTO.builder()
                .viewCount(viewCount)
                .thanksCount(thanks)
                .mentoringCount(mentoringCount)
                .bookmarked(null)
                .build();
    }

    private Pageable applySort(Pageable p, String sort) {
        if (sort == null || sort.isBlank() || "recent".equals(sort)) return p;
        return p; // popular/responseRate 컬럼 도입 전
    }

    private static String mask(String name) {
        if (name == null || name.isBlank()) return "";
        return name.substring(0, 1) + "OO";
    }
}