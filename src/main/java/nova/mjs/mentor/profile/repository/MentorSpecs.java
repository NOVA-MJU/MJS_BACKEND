package nova.mjs.mentor.profile.repository;

import jakarta.persistence.criteria.JoinType;
import nova.mjs.mentor.profile.entity.Mentor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/** 상태 없는 스펙 유틸: 상속/인스턴스화 방지를 위해 final + private ctor */
public final class MentorSpecs {

    private MentorSpecs() {}

    /** 회사/직무/설명 에 대한 키워드 검색 */
    public static Specification<Mentor> keyword(String q) {
        if (q == null || q.isBlank()) return null;
        String k = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("workplace")), k),
                cb.like(cb.lower(root.get("jobTitle")),  k),
                cb.like(cb.lower(root.get("description")), k)
        );
    }

    /** skills(ElementCollection<String>) ANY 매칭 */
    public static Specification<Mentor> hasAnySkill(List<String> skills) {
        if (skills == null || skills.isEmpty()) return null;
        return (root, cq, cb) -> {
            var join = root.join("skills", JoinType.LEFT);
            cq.distinct(true);
            return join.in(skills);
        };
    }
}
// 엔티티 필드에 맞춘 키워드/스킬 검색 스펙 Mentor 필드: workplace, jobTitle, description, skills(List<String>)
