package nova.mjs.admin.repository;

import nova.mjs.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    boolean existsByAdminId(String adminId);
    Optional<Admin> findByAdminId(String adminId);
}