package nova.mjs.domain.mentorship.program.repository;

import nova.mjs.domain.mentorship.program.entity.MentoringProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * MentoringProgram Repository
 *
 * - 내부 PK(id)는 JPA 관리
 * - 외부 접근은 UUID 기준
 * - ADMIN 프로그램 관리 전용
 */
@Repository
public interface MentoringProgramRepository
        extends JpaRepository<MentoringProgram, Long> {

    /**
     * UUID 기반 프로그램 단건 조회
     */
    Optional<MentoringProgram> findByUuid(UUID uuid);

    /**
     * 프로그램 상세 조회
     *
     * - 멘토 + 멘토 Member까지 fetch
     * - 상세 페이지 N+1 방지
     */
    @Query("""
        select distinct p
        from MentoringProgram p
        left join fetch p.mentors m
        left join fetch m.member
        where p.uuid = :uuid
    """)
    Optional<MentoringProgram> findDetailByUuid(@Param("uuid") UUID uuid);
}
