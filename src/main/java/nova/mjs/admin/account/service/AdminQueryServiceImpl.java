package nova.mjs.admin.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.repository.DepartmentRepository;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminQueryServiceImpl implements AdminQueryService {

    private static DepartmentRepository departmentRepository;

    // 관리자인지 확인하기
    // 1. 로그인한 회원 Email과 Deparment에 등록된 관리자와 동일한지 검증 메서드

    @Override
    public Boolean validateIsAdminOfDepartment(UserPrincipal userPrincipal, UUID departmentUuid) {
        String loginEmail = userPrincipal.getUsername();

        return departmentRepository.findAdminEmailByDepartmentUuid(departmentUuid)
                .map(adminEmail -> adminEmail.equals(loginEmail))
                .orElse(false);
    }

}
