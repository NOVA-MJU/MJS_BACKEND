package nova.mjs.admin.department.notice.service;

import nova.mjs.admin.department.notice.dto.AdminStudentCouncilNoticeDTO;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.util.security.UserPrincipal;

import java.util.UUID;

/**
 * 학생회(학과 관리자) 전용 공지사항 서비스
 *
 * 학과 서비스 단위(College + DepartmentName)를 기준으로
 * 공지사항 CRUD를 수행한다.
 */
public interface AdminStudentCouncilNoticeService {

    // 상세 조회
    AdminStudentCouncilNoticeDTO.Response getAdminDepartmentNoticeDetail(
            College college,
            DepartmentName departmentName,
            UUID noticeUuid,
            UserPrincipal userPrincipal
    );

    // 생성
    AdminStudentCouncilNoticeDTO.Response createNotice(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            AdminStudentCouncilNoticeDTO.Request request
    );

    // 수정
    AdminStudentCouncilNoticeDTO.Response updateNotice(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            UUID noticeUuid,
            AdminStudentCouncilNoticeDTO.Request request
    );

    // 삭제
    void deleteNotice(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            UUID noticeUuid
    );
}
