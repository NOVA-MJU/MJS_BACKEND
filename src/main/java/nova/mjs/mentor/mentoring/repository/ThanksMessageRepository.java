package nova.mjs.mentor.mentoring.repository;

import nova.mjs.mentor.mentoring.entity.ThanksMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThanksMessageRepository extends JpaRepository<ThanksMessage, Long> {

    // Mentoring -> Mentor 경유 조인 (스프링 데이터 네임드 경로)
    long countByMentoring_Mentor_Id(Long mentorId);
}
