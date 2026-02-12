package nova.mjs.admin.account.service;

import nova.mjs.admin.account.DTO.AdminDTO;
import nova.mjs.domain.thingo.department.dto.DepartmentDTO;
import nova.mjs.util.response.ApiResponse;

public interface AdminCommandService {

    /**
    1. 이메일을 우리한테 원하는걸로 주면 그걸로 우리가 객체 생성한다음에,
    2. 회원가입 할때 이메일 작성하고 이메일 검증 누르면 이메일 발송 인증이 아니라 우리가 만든 아이디인지 확인하기로 했고,
    3. 회원가입할 때 우리랑 소통할 수 있는 contactEmail을 작성해서 그렇게 소통하기

    # 관리자 회원가입
     1. 관리자 이메일 검증 메서드
     2. 관리자 정보 수정하기
    /*


    /**
     * 초기 학생회 관리자 계정 등록(OPERATOR)
     *
     * @param request 초기 등록 요청 DTO
     * @return 등록 성공 메시지
     */
    ApiResponse<String> registerInitAdmin(AdminDTO.StudentCouncilInitRegistrationRequestDTO request);

    /**
     * 관리자(학생회) 정보 수정
     *
     * @param request 수정 요청 DTO
     * @return 수정된 관리자 응답 DTO
     */
    AdminDTO.StudentCouncilResponseDTO updateAdmin(AdminDTO.StudentCouncilUpdateDTO request);

    /**
     * 주어진 이메일이 초기 관리자 계정인지 검증
     *
     * @param emailId 확인할 이메일 주소
     * @return 존재하면 true, 없으면 false
     */
    Boolean validationInitAdminID(String emailId);
}
