package nova.mjs.domain.thingo.member.service.command;

import nova.mjs.domain.thingo.member.DTO.MemberDTO;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.util.security.AuthDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MemberCommandService {

    /** S3 프로필 이미지 등록 (CloudFront URL 반환) */
    String uploadProfileImage(MultipartFile file);

    /** 회원가입 */
    AuthDTO.LoginResponseDTO registerMember(MemberDTO.MemberRegistrationRequestDTO request);

    /** 회원 정보 수정 */
    Member updateMember(String emailId, MemberDTO.MemberUpdateRequestDTO requestDTO);

    /** 비밀번호 변경 */
    void updatePassword(String emailId, MemberDTO.PasswordRequestDTO requestDTO);

    /** 회원 삭제 */
    void deleteMember(String emailId, MemberDTO.PasswordRequestDTO requestPassword);

    /** 비번찾기 2단계: 코드 검증 성공 시 내부 플래그 세팅 */
    void verifyCodeForRecovery(String email, String code);

    /** 비번찾기 3단계: 내부 플래그 확인되면 비밀번호 변경 */
    void resetPasswordAfterVerified(String email, String newPassword);
}
