package nova.mjs.mentor.profile.service;

import jakarta.validation.Valid;
import nova.mjs.domain.member.DTO.MemberDTO;
import nova.mjs.mentor.profile.dto.MentorProfileDTO;
import nova.mjs.mentor.profile.entity.Mentor;
import nova.mjs.util.security.AuthDTO;

public interface MentorProfileCommandService {

    /** 신규 회원 + 멘토 프로필 동시 등록 */
    AuthDTO.LoginResponseDTO registerMemberAndMentor(
            @Valid nova.mjs.mentor.profile.dto.MentorRegistrationDTO.Request request
    );

    /** 기존 회원에 멘토 프로필 추가 */
    Mentor addMentorProfileForExistingMember(String email, @Valid MentorProfileDTO.Request mentorReq);
}
