package nova.mjs.domain.thingo.department.repository;

import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentNoticeRepository extends JpaRepository<DepartmentNotice, Long> {

    /* 최신 공지 순 */
    Page<DepartmentNotice> findByDepartmentOrderByDateDesc(
            Department department,
            Pageable pageable
    );
}
