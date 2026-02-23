package nova.mjs.domain.thingo.department.service.notice;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.dto.DepartmentNoticeDTO;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.exception.CollegeNotFoundException;
import nova.mjs.domain.thingo.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.thingo.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentNoticeQueryServiceImpl implements DepartmentNoticeQueryService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentNoticeRepository noticeRepository;

    @Override
    public Page<DepartmentNoticeDTO.Summary> getDepartmentNoticePage(
            College college,
            DepartmentName departmentName,
            Pageable pageable
    ) {
        Department department = getDepartment(college, departmentName);

        return noticeRepository
                .findByDepartmentOrderByDateDesc(department, pageable)
                .map(DepartmentNoticeDTO.Summary::fromEntity);
    }

    private Department getDepartment(College college, DepartmentName departmentName) {
        if (departmentName == null) {
            return departmentRepository
                    .findCollegeLevelDepartment(college)
                    .orElseThrow(CollegeNotFoundException::new);
        }

        return departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(DepartmentNotFoundException::new);
    }
}
