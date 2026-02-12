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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentCouncilNoticeQueryServiceImpl implements StudentCouncilNoticeQueryService {

    private final DepartmentRepository departmentRepository;
    private final StudentCouncilNoticeRepository noticeRepository;

    /* 목록 */
    @Override
    public Page<StudentCouncilNoticeDTO.Summary> getNoticePage(
            College college,
            DepartmentName departmentName,
            Pageable pageable
    ) {
        Department department = getDepartment(college, departmentName);

        return noticeRepository
                .findByDepartmentOrderByPublishedAtDesc(department, pageable)
                .map(StudentCouncilNoticeDTO.Summary::fromEntity);
    }

    /* 상세 */
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

    private Department getDepartment(College college, DepartmentName departmentName) {
        return departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(DepartmentNotFoundException::new);
    }
}
