package nova.mjs.admin.account.service;

import nova.mjs.util.security.UserPrincipal;

import java.util.UUID;

public interface AdminQueryService {
    // 어드민 본인인지 조회
    Boolean validateIsAdminOfDepartment(UserPrincipal userPrincipal, UUID departmentUuid);

}
