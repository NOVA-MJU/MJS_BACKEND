package nova.mjs.mentor.profile.service.command;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.member.DTO.MemberDTO;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.repository.MemberRepository;
import nova.mjs.domain.member.service.query.MemberQueryService;
import nova.mjs.mentor.profile.dto.MentorProfileDTO;
import nova.mjs.mentor.profile.dto.MentorRegistrationDTO;
import nova.mjs.mentor.profile.entity.Mentor;
import nova.mjs.mentor.profile.repository.MentorRepository;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.exception.request.RequestException;
import nova.mjs.util.jwt.JwtUtil;
import nova.mjs.util.security.AuthDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MentorProfileCommandServiceImpl implements MentorProfileCommandService {

    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;
    private final MentorRepository mentorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 신규 회원가입 + 멘토 프로필 생성 (원자적 처리)
     */
    @Override
    public AuthDTO.LoginResponseDTO registerMemberAndMentor(@Valid MentorRegistrationDTO.Request request) {

        // 0) 회원 검증 (기존 로직 재사용)
        MemberDTO.MemberRegistrationRequestDTO memberReq = request.toMemberReq();
        memberQueryService.validateNicknameDuplication(memberReq.getNickname());
        memberQueryService.validateEmailDomain(memberReq.getEmail());
        memberQueryService.validateEmailDuplication(memberReq.getEmail());
        memberQueryService.validateStudentNumberDuplication(memberReq.getStudentNumber());

        // 1) Member 생성/저장
        String encoded = passwordEncoder.encode(memberReq.getPassword());
        Member member;
        try {
            member = memberRepository.save(Member.create(memberReq, encoded));
            // 필요 시: 멘토 가입 시 Role 설정이 있다면 여기서 처리
            // member.updateRole(Role.MENTOR);  (예시)
        } catch (DataIntegrityViolationException e) {
            // 이메일/닉네임/학번 UNIQUE 충돌 보호
            throw new RequestException(ErrorCode.INVALID_REQUEST);
        }

        // 2) Mentor 생성/저장
        MentorProfileDTO.Request mentorReq = request.toMentorReq();
        if (mentorRepository.existsByMember_Id(member.getId())) {
            throw new RequestException(ErrorCode.INVALID_REQUEST);
        }
        try {
            mentorRepository.save(Mentor.create(member, mentorReq));
        } catch (DataIntegrityViolationException e) {
            // uk_mentor_member 등 UNIQUE 제약
            throw new RequestException(ErrorCode.INVALID_REQUEST);
        }

        // 3) 토큰 발급 (MemberCommandServiceImpl 포맷 동일)
        UUID userId = member.getUuid();
        String email = member.getEmail();
        String role = String.valueOf(member.getRole());
        String accessToken = jwtUtil.generateAccessToken(userId, email, role);
        String refreshToken = jwtUtil.generateRefreshToken(userId, email);

        return AuthDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 기존 회원에 멘토 프로필 추가
     */
    @Override
    public Mentor addMentorProfileForExistingMember(String email, @Valid MentorProfileDTO.Request mentorReq) {
        // 1) 회원 조회
        Member member = memberQueryService.getMemberByEmail(email);

        // 2) 중복 체크
        if (mentorRepository.existsByMember_Id(member.getId())) {
            throw new RequestException(ErrorCode.INVALID_REQUEST);
        }

        // 3) 멘토 프로필 생성/저장
        try {
            return mentorRepository.save(Mentor.create(member, mentorReq));
        } catch (DataIntegrityViolationException e) {
            throw new RequestException(ErrorCode.INVALID_REQUEST);
        }
    }
}
