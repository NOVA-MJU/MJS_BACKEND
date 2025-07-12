package nova.mjs.admin.account.repository;

import nova.mjs.admin.account.entity.StudentCouncilAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<StudentCouncilAdmin, Long> {
    Optional<StudentCouncilAdmin> findByContactEmail(String adminId);
}