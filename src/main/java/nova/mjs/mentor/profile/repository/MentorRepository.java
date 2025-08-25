package nova.mjs.mentor.profile.repository;

import nova.mjs.mentor.profile.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
    boolean existsByMember_Id(Long memberId);
    Optional<Mentor> findByMember_Id(Long memberId);
}
