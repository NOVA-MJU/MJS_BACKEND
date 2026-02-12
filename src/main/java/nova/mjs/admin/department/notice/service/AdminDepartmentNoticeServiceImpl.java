package nova.mjs.admin.department.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.admin.account.service.AdminQueryService;
import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeRequestDTO;
import nova.mjs.admin.department.notice.dto.AdminDepartmentNoticeResponseDTO;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.exception.DepartmentAdminNotFoundException;
import nova.mjs.domain.thingo.department.exception.DepartmentNoticeNotFoundException;
import nova.mjs.domain.thingo.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3Service;
import nova.mjs.util.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class AdminDepartmentNoticeServiceImpl implements AdminDepartmentNoticeService {

    private final AdminQueryService adminQueryService;
    private final DepartmentRepository departmentRepository;
    private final DepartmentNoticeRepository departmentNoticeRepository;
    private final S3Service s3Service;

    private final String departmentNoticePrefix =
            S3DomainType.DEPARTMENT_NOTICE.getPrefix();

    /* ==========================================================
     * 1. 상세 조회
     *
     * 비즈니스 흐름:
     *  1) 해당 학과 존재 확인
     *  2) 관리자 권한 검증
     *  3) 해당 학과에 속한 공지인지 검증
     * ========================================================== */
    @Override
    public AdminDepartmentNoticeResponseDTO getAdminDepartmentNoticeDetail(
            College college,
            DepartmentName departmentName,
            UUID noticeUuid,
            UserPrincipal userPrincipal
    ) {
        DepartmentNotice notice =
                validateAdminAndGetNotice(college, departmentName, noticeUuid, userPrincipal);

        return AdminDepartmentNoticeResponseDTO.fromEntity(notice);
    }

    /* ==========================================================
     * 2. 생성
     *
     * 비즈니스 흐름:
     *  1) 학과 존재 확인
     *  2) 관리자 권한 확인
     *  3) 공지 엔티티 생성
     *  4) 저장
     * ========================================================== */
    @Override
    @Transactional
    public AdminDepartmentNoticeResponseDTO createNotice(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            AdminDepartmentNoticeRequestDTO request
    ) {
        Department department =
                validateAdminAndGetDepartment(userPrincipal, college, departmentName);

        DepartmentNotice notice = DepartmentNotice.create(request, department);
        departmentNoticeRepository.save(notice);

        log.info("[학과 공지 생성] college={}, department={}, noticeUuid={}",
                college, departmentName, notice.getUuid());

        return AdminDepartmentNoticeResponseDTO.fromEntity(notice);
    }

    /* ==========================================================
     * 3. 수정
     *
     * 비즈니스 흐름:
     *  1) 학과 존재 확인
     *  2) 관리자 권한 확인
     *  3) 해당 학과의 공지인지 확인
     *  4) 엔티티 수정 (Dirty Checking)
     * ========================================================== */
    @Override
    @Transactional
    public AdminDepartmentNoticeResponseDTO updateNotice(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            UUID noticeUuid,
            AdminDepartmentNoticeRequestDTO request
    ) {
        DepartmentNotice notice =
                validateAdminAndGetNotice(college, departmentName, noticeUuid, userPrincipal);

        notice.update(request);

        log.info("[학과 공지 수정] college={}, department={}, noticeUuid={}",
                college, departmentName, noticeUuid);

        return AdminDepartmentNoticeResponseDTO.fromEntity(notice);
    }

    /* ==========================================================
     * 4. 삭제
     *
     * 비즈니스 흐름:
     *  1) 학과 존재 확인
     *  2) 관리자 권한 확인
     *  3) 해당 학과 공지 확인
     *  4) S3 폴더 삭제
     *  5) DB 삭제
     * ========================================================== */
    @Override
    @Transactional
    public void deleteNotice(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName,
            UUID noticeUuid
    ) {
        DepartmentNotice notice =
                validateAdminAndGetNotice(college, departmentName, noticeUuid, userPrincipal);

        String folder = departmentNoticePrefix + notice.getUuid() + "/";
        s3Service.deleteFolder(folder);

        departmentNoticeRepository.delete(notice);

        log.info("[학과 공지 삭제] college={}, department={}, noticeUuid={}",
                college, departmentName, noticeUuid);
    }

    /* ==========================================================
     * 공통 내부 메서드
     * ========================================================== */

    /**
     * 관리자 권한 검증 + Department 조회
     */
    private Department validateAdminAndGetDepartment(
            UserPrincipal userPrincipal,
            College college,
            DepartmentName departmentName
    ) {
        boolean isAdmin =
                adminQueryService.validateIsAdminOfDepartment(
                        userPrincipal, college, departmentName
                );

        if (!isAdmin) {
            throw new DepartmentAdminNotFoundException();
        }

        return departmentRepository
                .findByCollegeAndDepartmentName(college, departmentName)
                .orElseThrow(DepartmentAdminNotFoundException::new);
    }

    /**
     * 관리자 검증 + 공지 조회
     */
    private DepartmentNotice validateAdminAndGetNotice(
            College college,
            DepartmentName departmentName,
            UUID noticeUuid,
            UserPrincipal userPrincipal
    ) {
        Department department =
                validateAdminAndGetDepartment(userPrincipal, college, departmentName);

        return departmentNoticeRepository
                .findByDepartmentAndUuid(department, noticeUuid)
                .orElseThrow(DepartmentNoticeNotFoundException::new);
    }
}
