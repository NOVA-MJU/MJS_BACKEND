package nova.mjs.admin.registration.service;

import nova.mjs.domain.department.entity.Department;
import nova.mjs.util.security.UserPrincipal;

import java.util.UUID;

public interface AdminQueryService {
    // 어드민 본인인지 조회
    Boolean validateIsAdminOfDepartment(UserPrincipal userPrincipal, UUID departmentUuid);
    // 회원 EmailId로 학과 찾기
    Department getDepartmentByAdminEmail(String emailId);
}
