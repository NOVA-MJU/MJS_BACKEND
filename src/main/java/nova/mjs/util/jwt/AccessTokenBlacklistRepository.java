package nova.mjs.util.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokenBlacklistRepository extends JpaRepository<AccessTokenBlacklist, Long> {
    boolean existsByAccessToken(String accessToken);


}