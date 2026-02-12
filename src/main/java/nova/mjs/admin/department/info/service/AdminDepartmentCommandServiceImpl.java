package nova.mjs.admin.department.info.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.thingo.department.dto.DepartmentDTO;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자용 학과 Command 서비스
 *
 * - 트랜잭션 경계 보유
 * - 유니크 선검증 수행
 * - 상태 변경 책임은 엔티티에 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminDepartmentCommandServiceImpl
        implements AdminDepartmentCommandService {

    private final DepartmentRepository departmentRepository;

    /* =========================
     * 생성
     * ========================= */
    @Override
    public DepartmentDTO.InfoResponse createDepartment(
            UserPrincipal userPrincipal,
            DepartmentDTO.CreateRequest request
    ) {

        validateUnique(request.getCollege(), request.getDepartmentName());

        Department department = Department.create(request);

        return DepartmentDTO.InfoResponse.fromEntity(
                departmentRepository.save(department)
        );
    }

    /* =========================
     * 수정
     * ========================= */
    @Override
    public DepartmentDTO.InfoResponse updateDepartment(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            DepartmentDTO.UpdateRequest request
    ) {

        Department department = findDepartment(college, departmentName);

        College newCollege =
                request.getCollege() != null ? request.getCollege() : department.getCollege();

        DepartmentName newDepartmentName =
                request.getDepartmentName() != null
                        ? request.getDepartmentName()
                        : department.getDepartmentName();

        // 변경되는 경우에만 유니크 검증
        if (!newCollege.equals(department.getCollege())
                || !equalsNullable(newDepartmentName, department.getDepartmentName())) {

            validateUnique(newCollege, newDepartmentName);
        }

        department.updateAdminInfo(request);

        return DepartmentDTO.InfoResponse.fromEntity(department);
    }

    private boolean equalsNullable(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /* =========================
     * 삭제
     * ========================= */
    @Override
    public void deleteDepartment(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName
    ) {

        Department department = findDepartment(college, departmentName);

        departmentRepository.delete(department);
    }

    /* =========================
     * 내부 공통 메서드
     * ========================= */

    private Department findDepartment(
            College college,
            DepartmentName departmentName
    ) {
        return departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 학과를 찾을 수 없습니다.")
                );
    }

    /**
     * 유니크 선검증
     *
     * departmentName이 null인 경우도 동일하게 체크한다.
     */
    private void validateUnique(
            College college,
            DepartmentName departmentName
    ) {

        boolean exists =
                departmentRepository.existsByCollegeAndDepartmentName(
                        college,
                        departmentName
                );

        if (exists) {
            throw new IllegalStateException(
                    "이미 존재하는 (College, DepartmentName) 조합입니다."
            );
        }
    }
}
