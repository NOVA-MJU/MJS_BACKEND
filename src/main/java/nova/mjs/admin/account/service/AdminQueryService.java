package nova.mjs.admin.account.service;

import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.util.security.UserPrincipal;

import java.util.UUID;

public interface AdminQueryService {
    /**
     * 로그인 사용자가 해당 학과의 관리자(학생회/교학팀)인지 검증
     */
    boolean validateIsAdminOfDepartment(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName
    );

    /**
     * 관리자 이메일 기준으로 담당 학과 조회
     */
    Department getDepartmentByAdminEmail(String emailId);

}
