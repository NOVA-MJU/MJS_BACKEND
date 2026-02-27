package nova.mjs.domain.thingo.department.service.notice;

import nova.mjs.domain.thingo.department.dto.DepartmentNoticeDTO;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentNoticeQueryService {
    /**
     * 학과별 공지 목록 조회 (페이지네이션)
     */
    Page<DepartmentNoticeDTO.Summary> getDepartmentNoticePage(
            College college,
            DepartmentName departmentName,
            Pageable pageable
    );

    /**
     * 학과/단과 공지 크롤링 후 저장(업서트)
     * - 운영/관리용
     */
    void crawlDepartmentNotices(College college, DepartmentName departmentName);

}
