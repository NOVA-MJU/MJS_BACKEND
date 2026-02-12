package nova.mjs.admin.department.info.service;

import nova.mjs.domain.thingo.department.dto.DepartmentDTO;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.util.security.UserPrincipal;

/**
 * 관리자용 학과 명령 서비스
 *
 * 생성 / 수정 / 삭제 책임을 가진다.
 */
public interface AdminDepartmentCommandService {

    /**
     * 학과 생성
     */
    DepartmentDTO.InfoResponse createDepartment(
            UserPrincipal userPrincipal,
            DepartmentDTO.CreateRequest request
    );

    /**
     * 학과 수정
     */
    DepartmentDTO.InfoResponse updateDepartment(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            DepartmentDTO.UpdateRequest request
    );


    /**
     * 학과 삭제
     */

    void deleteDepartment(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName
    );
}

