package nova.mjs.mentor.mentoring.repository;

import nova.mjs.mentor.mentoring.entity.Mentoring;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringRepository extends JpaRepository<Mentoring, Long> {

    long countByMentor_Id(Long mentorId);   // 멘토별 멘토링 수

    // 전체 누적 상담 수 (status 구분 없다면 전체 카운트)
    default long countAllConsultations() {
        return count();
    }
}
