package nova.mjs.domain.member.service.command;

import nova.mjs.domain.member.DTO.MemberDTO;
import nova.mjs.domain.member.entity.Member;
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
}
