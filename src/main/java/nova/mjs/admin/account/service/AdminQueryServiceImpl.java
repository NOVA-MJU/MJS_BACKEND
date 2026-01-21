package nova.mjs.admin.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.domain.thingo.member.exception.MemberNotFoundException;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminQueryServiceImpl implements AdminQueryService {

    private final DepartmentRepository departmentRepository;

    // 관리자인지 확인하기
    // 1. 로그인한 회원 Email과 Deparment에 등록된 관리자와 동일한지 검증 메서드

    @Override
    public Boolean validateIsAdminOfDepartment(UserPrincipal userPrincipal, UUID departmentUuid) {
        String loginEmail = userPrincipal.getUsername();

        return departmentRepository.findAdminEmailByDepartmentUuid(departmentUuid)
                .map(adminEmail -> adminEmail.equals(loginEmail))
                .orElse(false);
    }

    @Override
    public Department getDepartmentByAdminEmail(String emailId) {
        return departmentRepository.findByAdminEmail(emailId)
                .orElseThrow(MemberNotFoundException::new);
    }

}
