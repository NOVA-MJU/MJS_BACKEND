package nova.mjs.domain.thingo.department.service.notice;

import nova.mjs.domain.thingo.department.dto.DepartmentNoticesDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DepartmentNoticeQueryService {

    /** 학과별 공지 목록(페이지네이션) */
    Page<DepartmentNoticesDTO.Summary> getNoticePage(UUID departmentUuid, Pageable pageable);

    /** 공지 단건 상세 */
    DepartmentNoticesDTO.Detail getNoticeDetail(UUID departmentUuid, UUID noticeUuid);
}
