package nova.mjs.domain.department.exception;

import nova.mjs.domain.notice.exception.NoticeException;
import nova.mjs.util.exception.ErrorCode;

public class DepartmentNoticeNotFoundException extends DepartmentException {
    public DepartmentNoticeNotFoundException() {
        super(ErrorCode.DEPARTMENT_NOTICE_NOT_FOUND);
    }
}