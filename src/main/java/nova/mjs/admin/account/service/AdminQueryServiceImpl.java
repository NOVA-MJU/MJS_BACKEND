package nova.mjs.admin.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.thingo.department.repository.DepartmentAdminRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminQueryServiceImpl implements AdminQueryService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentAdminRepository departmentAdminRepository;

    /**
     * 로그인 사용자가 특정 학과의 관리자 여부 검증
     *
     * 기준:
     * - college + departmentName 으로 Department 조회
     * - Department.admin.email 과 로그인 email 비교
     */
    @Override
    public boolean validateIsAdminOfDepartment(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName
    ) {
        Department department = getDepartment(college, departmentName);

        return departmentAdminRepository.existsByDepartmentAndAdminEmail(
                department, userPrincipal.getUsername()
        );
    }

    /**
     * 관리자 이메일 기준으로 담당 Department 조회
     */
    @Override
    public Department getDepartmentByAdminEmail(String emailId) {
        return departmentAdminRepository.findFirstByAdminEmail(emailId)
                .map(departmentAdmin -> departmentAdmin.getDepartment())
                .orElseThrow(DepartmentNotFoundException::new);
    }

    /* ==================================================
     * 공통 내부 메서드
     * ================================================== */

    /**
     * college + departmentName 기준 Department 조회
     *
     * - departmentName == null → 단과대 학생회
     * - departmentName != null → 학과 학생회
     */
    private Department getDepartment(
            College college,
            DepartmentName departmentName) {

        if (departmentName == null) {
            return departmentRepository
                    .findByCollegeAndDepartmentNameIsNull(college)
                    .orElseThrow(DepartmentNotFoundException::new);
        }

        return departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(DepartmentNotFoundException::new);
    }
}
