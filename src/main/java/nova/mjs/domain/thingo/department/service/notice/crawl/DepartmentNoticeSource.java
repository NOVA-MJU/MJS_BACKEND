package nova.mjs.domain.thingo.department.service.notice.crawl;

import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;

public record DepartmentNoticeSource(
        College college,
        DepartmentName departmentName,
        String label,
        String url,
        DepartmentNoticeSourceType sourceType
) {
}
