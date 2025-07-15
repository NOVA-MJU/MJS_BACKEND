package nova.mjs.domain.department.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.department.DTO.DepartmentNoticesResponseDTO;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.entity.DepartmentNotice;
import nova.mjs.domain.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.department.repository.DepartmentRepository;
import nova.mjs.domain.department.DTO.DepartmentNoticesResponseDTO.DepartmentNoticeDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentNoticeService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentNoticeRepository deparmentNoticeRepository;

    public DepartmentNoticesResponseDTO getNotices(UUID departmentUuid) {
        Department department = departmentRepository
                .findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        List<DepartmentNotice> notices = deparmentNoticeRepository
                .findByDepartment_DepartmentUuid(departmentUuid);

        return DepartmentNoticesResponseDTO.fromNoticeList(department, notices);
    }

    // ▶ 페이징된 preview 리스트
    public Page<DepartmentNoticesResponseDTO.NoticeSimpleDTO> getNoticesPage(
            UUID departmentUuid, int page, int size
    ) {
        // 1) 학과 체크
        departmentRepository.findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        // 2) 페이징된 엔티티 조회
        Page<DepartmentNotice> notices = deparmentNoticeRepository
                .findByDepartment_DepartmentUuid(departmentUuid, PageRequest.of(page, size));

        // 3) DTO 변환
        return notices.map(DepartmentNoticesResponseDTO.NoticeSimpleDTO::fromNoticeEntityPreview);
    }

    // ▶ 상세 content 조회 (토글)
    public DepartmentNoticeDetailDTO getNoticeDetail(UUID departmentUuid, UUID noticeUuid) {
        departmentRepository.findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        DepartmentNotice notice = deparmentNoticeRepository
                .findByDepartment_DepartmentUuidAndDepartmentNoticeUuid(departmentUuid, noticeUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        return DepartmentNoticeDetailDTO.of(notice);
    }
}
