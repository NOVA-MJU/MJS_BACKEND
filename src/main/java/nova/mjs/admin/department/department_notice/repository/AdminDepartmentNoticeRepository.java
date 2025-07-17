package nova.mjs.admin.department.department_notice.repository;

import nova.mjs.domain.department.entity.DepartmentNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminDepartmentNoticeRepository extends JpaRepository<DepartmentNotice, Long> {

    // ↓ 아래처럼 이름을 바꿔 주세요
    Optional<DepartmentNotice> findByDepartmentNoticeUuid(UUID departmentNoticeUuid);

    // 기존
    Optional<DepartmentNotice> findByDepartment_DepartmentUuidAndDepartmentNoticeUuid(
            UUID departmentUuid,
            UUID noticeUuid
    );
}
