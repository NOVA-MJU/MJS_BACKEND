package nova.mjs.admin.department.notice.service;

import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeRequestDTO;
import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeResponseDTO;
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
    AdminDepartmentNoticeResponseDTO createNotice(UserPrincipal userPrincipal, UUID departmentUuid, UUID noticeUuid, AdminDepartmentNoticeRequestDTO request);
    // 공지사항 업데이트
    AdminDepartmentNoticeResponseDTO updateNotice(UserPrincipal userPrincipal, UUID departmentUuid,
                                                  UUID noticeUuid, AdminDepartmentNoticeRequestDTO request);
    // 공지사항 삭제
    void deleteNotice(UUID departmentNoticeUuid, UUID departmentUuid, UserPrincipal userPrincipal);
}