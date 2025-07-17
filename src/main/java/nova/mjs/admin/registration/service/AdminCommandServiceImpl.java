package nova.mjs.admin.registration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.admin.registration.DTO.AdminDTO;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.department.repository.DepartmentRepository;
import nova.mjs.domain.member.DTO.MemberDTO;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.exception.MemberNotFoundException;
import nova.mjs.domain.member.repository.MemberRepository;
import nova.mjs.domain.member.service.query.MemberQueryService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class AdminCommandServiceImpl implements AdminCommandService {
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberQueryService memberQueryService;
    private final AdminQueryService adminQueryService;
    private final MemberRepository memberRepository;



    /**
     * 회원 가입 로직
     */
    // 1. OPERATOR(시스템 관리자)가 초기 어드민 객체 생성.
    @Transactional
    public ApiResponse<String> registerInitAdmin(AdminDTO.StudentCouncilInitRegistrationRequestDTO request) {
        // 회원이 입력한 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 이메일 중복 확인
        memberQueryService.validateEmailDuplication(request.getEmail());

        // 회원객체 생성
        Member newMember = Member.createAdminInit(request, encodedPassword);
        Department department = Department.createWithAdmin(request, newMember);
        memberRepository.save(newMember);
        departmentRepository.save(department);


        // 응답 DTO 반환
        return ApiResponse.success("초기 어드민 계정이 성공적으로 등록되었습니다.");
    }

    /**
     * 학생회(ADMIN) 계정 + 학과 정보를 동시에 수정
     *
     * @param request 수정 요청 DTO
     * @return 수정 완료된 Member 엔티티
     */
    @Transactional
    public AdminDTO.StudentCouncilResponseDTO updateAdmin(AdminDTO.StudentCouncilUpdateDTO request) {
        // 1. 관리자 계정 조회
        Member member = memberQueryService.getMemberByEmail(request.getEmail()); // 이메일은 불변값이라 기준으로 삼음

        // 3. 회원(Member) 정보 업데이트
        member.updateAdmin(request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            member.updatePassword(encodedPassword);
        }

        Department department = adminQueryService.getDepartmentByAdminEmail(member.getEmail());
        department.updateInfo(request);

        return AdminDTO.StudentCouncilResponseDTO.fromEntity(member, department);
    }



    public Boolean validationInitAdminID(String emailId) {
        try {
            Member member = memberQueryService.getMemberByEmail(emailId);
            return true;
        } catch (MemberNotFoundException e) {
            return false; // 회원이 없으면 검증 실패
        }
    }


}