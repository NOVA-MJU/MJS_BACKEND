// src/main/java/nova/mjs/department/service/DepartmentNoticeService.java
package nova.mjs.department.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.department.DTO.DepartmentNoticeDTO;
import nova.mjs.department.DTO.DepartmentNoticeResponseDTO;
import nova.mjs.department.DTO.DepartmentInfoDTO;
import nova.mjs.department.repository.DepartmentNoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentNoticeService {

    private final DepartmentNoticeRepository noticeRepository;
    private final DepartmentService departmentService;

    public DepartmentNoticeResponseDTO getNoticesByDepartmentUuid(UUID departmentUuid) {
        DepartmentInfoDTO info = departmentService.getDepartmentInfo(departmentUuid);

        var notices = noticeRepository
                .findByDepartment_DepartmentUuid(departmentUuid)
                .stream()
                .map(DepartmentNoticeDTO::of)
                .toList();

        return DepartmentNoticeResponseDTO.builder()
                .departmentInfo(info)
                .notices(notices)
                .build();
    }
}
