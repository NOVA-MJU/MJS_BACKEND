package nova.mjs.mentor.profile.repository;

import nova.mjs.mentor.profile.entity.Mentor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MentorRepository extends JpaRepository<Mentor, Long>, JpaSpecificationExecutor<Mentor> {

    boolean existsByMember_Id(Long memberId);

    Optional<Mentor> findByMember_Id(Long memberId);

    @EntityGraph(attributePaths = {"careers", "portfolios"})
    Optional<Mentor> findWithDetailsById(Long id);

    Optional<Mentor> findByMember_Uuid(UUID memberUuid);

    @EntityGraph(attributePaths = {"careers", "portfolios"})
    Optional<Mentor> findWithDetailsByMember_Uuid(UUID memberUuid);
}
// Long 기반 ID, Member와 1:1(Unique)관계 유지. 스펙 검색과 상세 Fetch 조인을 위해 JpaSpecificationExecutor 및 @EntityGraph 메서드 추가