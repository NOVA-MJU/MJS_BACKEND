package nova.mjs.domain.mentorship.application.repository;

import nova.mjs.domain.mentorship.application.entity.MentorshipApplication;
import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MentorshipApplicationRepository
        extends JpaRepository<MentorshipApplication, Long> {

    Optional<MentorshipApplication> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"applicant", "mentor"})
    Page<MentorshipApplication> findByApplicant(Member applicant, Pageable pageable);

    @EntityGraph(attributePaths = {"applicant", "mentor"})
    Page<MentorshipApplication> findByMentor(Member mentor, Pageable pageable);
}
