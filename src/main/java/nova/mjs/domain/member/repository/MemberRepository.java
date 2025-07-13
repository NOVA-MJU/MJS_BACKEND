package nova.mjs.domain.member.repository;


import nova.mjs.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
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


}
