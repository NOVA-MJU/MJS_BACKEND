package nova.mjs.domain.thingo.department.repository;

import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 학과 공지사항 Repository (V2 기준)
 *
 * 학과 식별은 UUID가 아닌 Department 객체 기반으로 조회한다.
 */
public interface DepartmentNoticeRepository extends JpaRepository<DepartmentNotice, Long> {

    /**
     * 학과 기준 공지 목록 조회
     */
    Page<DepartmentNotice> findByDepartment(
            Department department,
            Pageable pageable
    );

    /**
     * 공지 UUID 단일 조회
     */
    Optional<DepartmentNotice> findByUuid(UUID uuid);

    /**
     * 학과 + 공지 UUID 기준 조회
     *
     * - 권한 검증 및 데이터 무결성 보장
     */
    Optional<DepartmentNotice> findByDepartmentAndUuid(
            Department department,
            UUID uuid
    );
}
