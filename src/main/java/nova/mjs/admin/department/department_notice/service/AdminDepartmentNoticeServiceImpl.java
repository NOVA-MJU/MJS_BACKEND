package nova.mjs.admin.department.department_notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.admin.account.service.AdminQueryService;
import nova.mjs.admin.department.department_notice.dto.AdminDepartmentNoticeRequestDTO;
import nova.mjs.admin.department.department_notice.dto.AdminDepartmentNoticeResponseDTO;
import nova.mjs.admin.department.department_notice.repository.AdminDepartmentNoticeRepository;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.entity.DepartmentNotice;
import nova.mjs.domain.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.department.repository.DepartmentRepository;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3Service;
import nova.mjs.admin.department.department_notice.service.AdminDepartmenetNoticeService;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nova.mjs.domain.department.exception.DepartmentNotFoundException;
import nova.mjs.domain.department.exception.DepartmentException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class AdminDepartmentNoticeServiceImpl implements AdminDepartmenetNoticeService {

    private final AdminQueryService adminQueryService;
    private final DepartmentRepository departmentRepository;
    private final DepartmentNoticeRepository departmentNoticeRepository;
    private final S3Service s3Service;
    private final AdminDepartmentNoticeRepository adminDepartmentNoticeRepository;
    // S3 이미지가 들어가는 prefix (옵션)
    private final String departmentNoticePrefix = S3DomainType.DEPARTMENT_NOTICE.getPrefix();

    // 1) 상세 조회
    @Override
    public AdminDepartmentNoticeResponseDTO getAdminDepartmentNoticeDetail(
            UUID noticeUuid,
            UUID departmentUuid,
            UserPrincipal userPrincipal
    ) {
        DepartmentNotice notice = getExistingNotice(departmentUuid, noticeUuid, userPrincipal);
        return AdminDepartmentNoticeResponseDTO.fromEntity(notice);
    }

    // 2) 생성
    @Override
    @Transactional
    public AdminDepartmentNoticeResponseDTO createNotice(
            AdminDepartmentNoticeRequestDTO request,
            UUID departmentUuid,
            UserPrincipal userPrincipal
    ) {
        // 1) 학과 확인
        Department department = departmentRepository.findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        // 2) 관리자 권한 확인
        if (!adminQueryService.validateIsAdminOfDepartment(userPrincipal, departmentUuid)) {
            throw new IllegalArgumentException("해당 학과 관리자가 아닙니다.");
        }

        // 3) 새 UUID 부여 & 엔티티 생성
        UUID newNoticeUuid = UUID.randomUUID();
        DepartmentNotice notice = DepartmentNotice.create(
                newNoticeUuid,
                /* thumbnailUrl */ null,
                department,
                request.getTitle(),
                request.getContent()
        );

        // 4) 저장
        departmentNoticeRepository.save(notice);
        log.info("[학과 공지 생성] dept={}, noticeUuid={}", departmentUuid, newNoticeUuid);

        return AdminDepartmentNoticeResponseDTO.fromEntity(notice);
    }

    // 3) 수정
    @Override
    @Transactional
    public AdminDepartmentNoticeResponseDTO updateNotice(
            UUID noticeUuid,
            AdminDepartmentNoticeRequestDTO request,
            UUID departmentUuid,
            UserPrincipal userPrincipal
    ) {
        DepartmentNotice notice = getExistingNotice(departmentUuid, noticeUuid, userPrincipal);

        // 업데이트 메서드(엔티티 내부에서 previewContent 재생성)
        notice.update(
                /* thumbnailUrl */ null,
                request.getTitle(),
                request.getContent()
        );
        log.info("[학과 공지 수정] dept={}, noticeUuid={}", departmentUuid, noticeUuid);
        return AdminDepartmentNoticeResponseDTO.fromEntity(notice);
    }

    // 4) 삭제
    @Override
    @Transactional
    public void deleteNotice(
            UUID noticeUuid,
            UUID departmentUuid,
            UserPrincipal userPrincipal
    ) {
        DepartmentNotice notice = getExistingNotice(departmentUuid, noticeUuid, userPrincipal);

        // S3 에 저장된 이미지 폴더가 있다면 삭제 (옵션)
        String folder     = departmentNoticePrefix + notice.getDepartmentNoticeUuid() + "/";
        s3Service.deleteFolder(folder);

        departmentNoticeRepository.delete(notice);
        log.info("[학과 공지 삭제] dept={}, noticeUuid={}", departmentUuid, noticeUuid);
    }

    // —————————————————————————————————————————————————————————
    /**
     * 공통으로 쓰이는 “존재 확인 & 관리자 권한 확인” 헬퍼
     */
    private DepartmentNotice getExistingNotice(
            UUID departmentUuid,
            UUID noticeUuid,
            UserPrincipal userPrincipal
    ) {
        // 1) 학과 존재 체크
        departmentRepository.findByDepartmentUuid(departmentUuid)
                .orElseThrow(DepartmentNotFoundException::new);

        // 2) 관리자 권한 체크
        if (!adminQueryService.validateIsAdminOfDepartment(userPrincipal, departmentUuid)) {
            throw new IllegalArgumentException("해당 학과 관리자가 아닙니다.");
        }

        // 3) 공지 조회 (학과 + UUID)
        return adminDepartmentNoticeRepository
                .findByDepartment_DepartmentUuidAndDepartmentNoticeUuid(departmentUuid, noticeUuid)
                .orElseThrow(DepartmentNotFoundException::new);
    }

}
