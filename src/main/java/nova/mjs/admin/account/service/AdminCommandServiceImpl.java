package nova.mjs.admin.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.admin.account.DTO.AdminDTO;
import nova.mjs.admin.department.info.service.AdminDepartmentCommandService;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.mapping.DepartmentAdmin;
import nova.mjs.domain.thingo.department.repository.DepartmentAdminRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.exception.MemberNotFoundException;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import nova.mjs.domain.thingo.member.service.query.MemberQueryService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class AdminCommandServiceImpl implements AdminCommandService {

    private final PasswordEncoder passwordEncoder;
    private final DepartmentAdminRepository departmentAdminRepository;
    private final MemberQueryService memberQueryService;
    private final AdminDepartmentCommandService adminDepartmentCommandService;
    private final MemberRepository memberRepository;

    /**
     * 초기 학생회 관리자 계정 등록 (OPERATOR 전용)
     *
     * - Member만 생성
     * - Department 연결은 추후 생성 로직에서 assignAdmin() 처리
     */
    @Transactional
    public ApiResponse<String> registerInitAdmin(
            AdminDTO.StudentCouncilInitRegistrationRequestDTO request) {

        // 1. 이메일 중복 확인
        memberQueryService.validateEmailDuplication(request.getEmail());

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Member 생성
        Member admin = Member.createAdminInit(request, encodedPassword);
        memberRepository.save(admin);

        // 4. 학과 확인
        Department department = adminDepartmentCommandService.findDepartment(request.getCollege(), request.getDepartmentName());

        // 5. 연관관계 설정 (연관관계의 주인 쪽에서 세팅)
        department.assignAdmin(admin);


        return ApiResponse.success("초기 관리자 등록 완료");
    }

    /**
     * 학생회(ADMIN) 계정 정보 수정
     *
     * - Member 정보 수정
     * - 연결된 Department 존재 시 함께 수정
     */
    @Transactional
    public AdminDTO.StudentCouncilResponseDTO updateAdmin(
            AdminDTO.StudentCouncilUpdateDTO request) {

        // 1. 관리자 계정 조회
        Member member = memberQueryService.getMemberByEmail(request.getEmail());

        // 2. Member 정보 수정
        member.updateAdmin(request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            member.updatePassword(encodedPassword);
        }

        // 3. 연결된 Department 조회 (없을 수 있음)
        DepartmentAdmin departmentAdmin = departmentAdminRepository
                .findFirstByAdminEmail(member.getEmail())
                .orElse(null);

        Department department = null;
        if (departmentAdmin != null) {
            department = departmentAdmin.getDepartment();
            department.updateInfo(request);
        }

        return AdminDTO.StudentCouncilResponseDTO.fromEntity(member, department);
    }

    /**
     * 주어진 이메일이 관리자 계정인지 검증
     */
    public Boolean validationInitAdminID(String emailId) {
        try {
            memberQueryService.getMemberByEmail(emailId);
            return true;
        } catch (MemberNotFoundException e) {
            return false;
        }
    }
}
