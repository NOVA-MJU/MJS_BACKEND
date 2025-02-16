package nova.mjs.member;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUuid(UUID uuid);
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);

}
