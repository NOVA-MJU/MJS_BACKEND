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

    // ▶ 상세 토글용 단일 공지 조회
    Optional<DepartmentNotice> findByDepartment_DepartmentUuidAndDepartmentNoticeUuid(
            UUID departmentUuid, UUID noticeUuid);
}