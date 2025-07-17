package nova.mjs.admin.department.department_notice.service;

import nova.mjs.admin.department.department_notice.dto.AdminDepartmentNoticeRequestDTO;
import nova.mjs.admin.department.department_notice.dto.AdminDepartmentNoticeResponseDTO;
import nova.mjs.domain.member.entity.enumList.DepartmentName;
import nova.mjs.util.security.UserPrincipal;

import java.util.UUID;

/**
 * 학생회 공지사항 서비스 인터페이스
 *
 * 학생회 공지사항의 CRUD 기능 정의
 */
public interface AdminDepartmentNoticeService {

    // 공지사항 상세 조회
    AdminDepartmentNoticeResponseDTO getAdminDepartmentNoticeDetail(UUID departmentNoticeUuid, UUID departmentUuid, UserPrincipal userPrincipal);
    // 공지사항 생성
    AdminDepartmentNoticeResponseDTO createNotice(AdminDepartmentNoticeRequestDTO request, UUID departmentUuid, UserPrincipal userPrincipal);
    // 공지사항 업데이트
    AdminDepartmentNoticeResponseDTO updateNotice(UUID departmentNoticeUuid, AdminDepartmentNoticeRequestDTO request, UUID departmnentUuid, UserPrincipal userPrincipal);
    // 공지사항 삭제
    void deleteNotice(UUID departmentNoticeUuid, UUID departmentUuid, UserPrincipal userPrincipal);
}