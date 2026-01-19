package nova.mjs.domain.thingo.department.service.notice;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.dto.DepartmentNoticesDTO;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import nova.mjs.domain.thingo.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.thingo.department.exception.DepartmentNoticeNotFoundException;
import nova.mjs.domain.thingo.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentNoticeQueryServiceImpl implements DepartmentNoticeQueryService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentNoticeRepository noticeRepository;

    /* ------------------ public ------------------ */

    @Override
    public Page<DepartmentNoticesDTO.Summary> getNoticePage(UUID departmentUuid, Pageable pageable) {
        validateDepartmentExists(departmentUuid);

        return noticeRepository
                .findByDepartment_DepartmentUuid(departmentUuid, pageable)
                .map(DepartmentNoticesDTO.Summary::fromEntity);
    }

    @Override
    public DepartmentNoticesDTO.Detail getNoticeDetail(UUID departmentUuid, UUID noticeUuid) {
        validateDepartmentExists(departmentUuid);

        DepartmentNotice notice = noticeRepository
                .findByDepartment_DepartmentUuidAndUuid(departmentUuid, noticeUuid)
                .orElseThrow(DepartmentNoticeNotFoundException::new);

        return DepartmentNoticesDTO.Detail.fromEntity(notice);
    }

    /* ------------------ private ------------------ */

    private void validateDepartmentExists(UUID departmentUuid) {
        departmentRepository.findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);
    }
}
