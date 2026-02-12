package nova.mjs.domain.thingo.department.service.notice;

import nova.mjs.domain.thingo.department.dto.StudentCouncilNoticeDTO;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentCouncilNoticeQueryService {

    /**
     * 학과별 공지 목록 조회 (페이지네이션)
     */
    Page<StudentCouncilNoticeDTO.Summary> getNoticePage(
            College college,
            DepartmentName departmentName,
            Pageable pageable
    );

    /**
     * 학과별 공지 상세 조회
     */
    StudentCouncilNoticeDTO.Detail getNoticeDetail(
            College college,
            DepartmentName departmentName,
            UUID noticeUuid
    );
}
