package nova.mjs.mentor.profile.repository;

import nova.mjs.mentor.profile.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
    boolean existsByMember_Id(Long memberId);
    Optional<Mentor> findByMember_Id(Long memberId);

    Optional<Mentor> findByMember_Uuid(UUID memberUuid);
}
