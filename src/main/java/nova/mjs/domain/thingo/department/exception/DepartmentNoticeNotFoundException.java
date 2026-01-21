package nova.mjs.domain.thingo.department.exception;

import nova.mjs.util.exception.ErrorCode;

public class DepartmentNoticeNotFoundException extends DepartmentException {
    public DepartmentNoticeNotFoundException() {
        super(ErrorCode.DEPARTMENT_NOTICE_NOT_FOUND);
    }
}