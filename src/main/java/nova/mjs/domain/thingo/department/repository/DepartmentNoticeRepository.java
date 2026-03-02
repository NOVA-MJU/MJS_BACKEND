package nova.mjs.domain.thingo.department.repository;

import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


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

    @Query(
            value = """
                    select n
                    from DepartmentNotice n
                    where n.department.college = :college
                      and (
                            n.department.departmentName is null
                            or n.department.departmentName = :departmentName
                          )
                    order by n.date desc, n.id desc
                    """,
            countQuery = """
                    select count(n)
                    from DepartmentNotice n
                    where n.department.college = :college
                      and (
                            n.department.departmentName is null
                            or n.department.departmentName = :departmentName
                          )
                    """
    )
    Page<DepartmentNotice> findCollegeAndDepartmentLevelNotices(
            @Param("college") College college,
            @Param("departmentName") DepartmentName departmentName,
            Pageable pageable
    );

    long countByDepartment(Department department);

    /* 크롤링 시 학과 확인*/
    boolean existsByDepartmentAndLink(Department department, String link);

    Optional<DepartmentNotice> findByDepartmentNoticeUuid(UUID departmentNoticeUuid);

    void deleteByDepartment(Department department);

    void deleteByDepartmentIn(Collection<Department> departments);
}
