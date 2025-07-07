package nova.mjs.member.service;

import nova.mjs.member.Member;
import nova.mjs.member.MemberDTO;
import nova.mjs.util.security.AuthDTO;

/**
 * 회원 변경 서비스 인터페이스
 * CQRS 패턴의 Command 부분을 담당
 */
public interface MemberCommandService {
    
    /**
     * 회원 가입
     */
    AuthDTO.LoginResponseDTO registerMember(MemberDTO.MemberRequestDTO requestDTO);
    
    /**
     * 회원 정보 수정
     */
    Member updateMember(String emailId, MemberDTO requestDTO);
    
    /**
     * 비밀번호 변경
     */
    void updatePassword(String emailId, MemberDTO.PasswordRequestDTO requestDTO);
    
    /**
     * 회원 탈퇴
     */
    void deleteMember(String emailId, MemberDTO.PasswordRequestDTO requestPassword);
}