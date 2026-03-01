package nova.mjs.domain.thingo.department.repository;

import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentNoticeRepository extends JpaRepository<DepartmentNotice, Long> {

    /* 최신 공지 순 */
    Page<DepartmentNotice> findByDepartmentOrderByDateDesc(
            Department department,
            Pageable pageable
    );

    /* 크롤링 시 학과 확인*/
    boolean existsByDepartmentAndLink(Department department, String link);

    Optional<DepartmentNotice> findByDepartmentNoticeUuid(UUID departmentNoticeUuid);

    void deleteByDepartment(Department department);

    void deleteByDepartmentIn(Collection<Department> departments);
}
