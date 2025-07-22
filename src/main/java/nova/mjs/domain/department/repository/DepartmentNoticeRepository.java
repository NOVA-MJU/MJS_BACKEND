package nova.mjs.domain.department.repository;

import nova.mjs.domain.department.entity.DepartmentNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentNoticeRepository extends JpaRepository<DepartmentNotice, Long> {
    List<DepartmentNotice> findByDepartment_DepartmentUuid(UUID departmentUuid);
    Page<DepartmentNotice> findByDepartment_DepartmentUuid(UUID departmentUuid, Pageable pageable);

    /** 단일 UUID 기준 조회 (고유 식별자) */
    Optional<DepartmentNotice> findByUuid(UUID uuid);

    /** 학과 UUID와 공지 UUID 기준 조회 */
    Optional<DepartmentNotice> findByDepartment_DepartmentUuidAndUuid(UUID departmentUuid, UUID noticeUuid);

}