package nova.mjs.admin.department.department_notice.repository;

import nova.mjs.domain.department.entity.DepartmentNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminDepartmentNoticeRepository extends JpaRepository<DepartmentNotice, Long> {
    Optional<DepartmentNotice> findByUuid(UUID uuid);

    // 학과UUID & 공지UUID 로 단일 공지 찾기
    Optional<DepartmentNotice> findByDepartment_DepartmentUuidAndDepartmentNoticeUuid(
            UUID departmentUuid,
            UUID noticeUuid
    );

}
