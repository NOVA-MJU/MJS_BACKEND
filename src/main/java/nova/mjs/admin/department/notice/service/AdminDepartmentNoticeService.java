package nova.mjs.admin.department.notice.service;

import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeRequestDTO;
import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeResponseDTO;
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
public interface AdminDepartmentNoticeService {

    // 상세 조회
    AdminDepartmentNoticeResponseDTO getAdminDepartmentNoticeDetail(
            College college,
            DepartmentName departmentName,
            UUID noticeUuid,
            UserPrincipal userPrincipal
    );

    // 생성
    AdminDepartmentNoticeResponseDTO createNotice(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            AdminDepartmentNoticeRequestDTO request
    );

    // 수정
    AdminDepartmentNoticeResponseDTO updateNotice(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            UUID noticeUuid,
            AdminDepartmentNoticeRequestDTO request
    );

    // 삭제
    void deleteNotice(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            UUID noticeUuid
    );
}
