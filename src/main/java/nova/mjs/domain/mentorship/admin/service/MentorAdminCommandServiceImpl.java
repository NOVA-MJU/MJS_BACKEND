package nova.mjs.domain.mentorship.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.mentorship.admin.dto.MentorAdminDTO;
import nova.mjs.domain.mentorship.mentor.entity.MentorProfile;
import nova.mjs.domain.mentorship.mentor.repository.MentorProfileRepository;
import nova.mjs.domain.thingo.member.entity.Member;
import nova.mjs.domain.thingo.member.entity.Member.Role;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.member.repository.MemberRepository;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.exception.request.RequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MentorAdminCommandServiceImpl implements MentorAdminCommandService {

    private final MemberRepository memberRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MentorAdminDTO.Response registerMentor(MentorAdminDTO.Request request) {

        // 1. 이메일 중복 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RequestException(ErrorCode.DUPLICATE_EMAIL_EXCEPTION);
        }

        // 2. Member 생성 (role = MENTOR)
        Member mentorMember = Member.builder()
                .uuid(UUID.randomUUID())
                .name(request.getName())
                .email(request.getEmail())
                .departmentName(DepartmentName.OTHER)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.MENTOR)
                .build();

        memberRepository.save(mentorMember);

        // 3. MentorProfile 생성
        MentorProfile mentorProfile = MentorProfile.create(
                mentorMember,
                request.getDisplayName(),
                request.getDepartmentName(),
                request.getIntroduction(),
                request.getProfileImageUrl()
        );

        mentorProfileRepository.save(mentorProfile);

        // 4. 응답
        return MentorAdminDTO.Response.builder()
                .memberId(mentorMember.getId())
                .mentorProfileId(mentorProfile.getId())
                .email(mentorMember.getEmail())
                .displayName(mentorProfile.getDisplayName())
                .build();
    }
}
