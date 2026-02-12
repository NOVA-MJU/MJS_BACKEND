package nova.mjs.domain.thingo.department.service.notice;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.dto.StudentCouncilNoticeDTO;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.StudentCouncilNotice;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.thingo.department.exception.DepartmentNoticeNotFoundException;
import nova.mjs.domain.thingo.department.repository.StudentCouncilNoticeRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 학과 공지 조회 서비스 (읽기 전용)
 *
 * 비즈니스 규칙:
 *  - 학과는 College + DepartmentName으로 식별
 *  - 공지는 해당 학과에 속한 것만 조회 가능
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentCouncilNoticeQueryServiceImpl implements StudentCouncilNoticeQueryService {

    private final DepartmentRepository departmentRepository;
    private final StudentCouncilNoticeRepository noticeRepository;

    /* ==========================================================
     * 공지 목록 조회
     * ========================================================== */
    @Override
    public Page<StudentCouncilNoticeDTO.Summary> getNoticePage(
            College college,
            DepartmentName departmentName,
            Pageable pageable
    ) {
        Department department = getDepartment(college, departmentName);

        return noticeRepository
                .findByDepartment(department, pageable)
                .map(StudentCouncilNoticeDTO.Summary::fromEntity);
    }

    /* ==========================================================
     * 공지 상세 조회
     *
     * 단건 조회는 noticeUuid 기준으로 조회하되,
     * 해당 공지가 특정 학과에 속해있는지 검증한다.
     * ========================================================== */
    @Override
    public StudentCouncilNoticeDTO.Detail getNoticeDetail(
            College college,
            DepartmentName departmentName,
            UUID noticeUuid
    ) {
        Department department = getDepartment(college, departmentName);

        StudentCouncilNotice notice = noticeRepository
                .findByDepartmentAndUuid(department, noticeUuid)
                .orElseThrow(DepartmentNoticeNotFoundException::new);

        return StudentCouncilNoticeDTO.Detail.fromEntity(notice);
    }

    /* ==========================================================
     * 공통 내부 메서드
     * ========================================================== */

    private Department getDepartment(
            College college,
            DepartmentName departmentName
    ) {
        return departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(DepartmentNotFoundException::new);
    }
}
