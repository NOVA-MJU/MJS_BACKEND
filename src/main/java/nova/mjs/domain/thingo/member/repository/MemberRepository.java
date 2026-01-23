package nova.mjs.domain.thingo.member.repository;


import nova.mjs.domain.thingo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUuid(UUID uuid);
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByStudentNumber(String studentNumber);

    @Query("select m.id from Member m where m.uuid = :uuid")
    Optional<Long> findIdByUuid(@Param("uuid") UUID uuid);

}
