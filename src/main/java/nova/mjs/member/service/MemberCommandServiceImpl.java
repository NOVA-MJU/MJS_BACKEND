package nova.mjs.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.member.Member;
import nova.mjs.member.MemberDTO;
import nova.mjs.member.MemberRepository;
import nova.mjs.member.exception.*;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.exception.request.RequestException;
import nova.mjs.util.jwt.JwtUtil;
import nova.mjs.util.security.AuthDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 회원 변경 서비스 구현체
 * CQRS 패턴의 Command 부분을 담당
 */
@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthDTO.LoginResponseDTO registerMember(MemberDTO.MemberRequestDTO requestDTO) {
        validateEmail(requestDTO.getEmail());

        if (requestDTO.getNickname() == null) {
            throw new NicknameIsInvalidException();
        }
        if (memberRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateEmailException();
        }

        // 닉네임 중복 체크
        if (requestDTO.getNickname() != null && memberRepository.existsByNickname(requestDTO.getNickname())) {
            throw new DuplicateNicknameException();
        }

        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());
        Member newMember = Member.create(requestDTO, encodedPassword);
        newMember = memberRepository.save(newMember);

        // 회원 정보에서 UUID, 이메일, 기본 Role 가져오기
        UUID userId = newMember.getUuid();
        String email = newMember.getEmail();
        String role = String.valueOf(newMember.getRole()); // Member 엔티티에 role 필드가 있어야 함

        // Access Token & Refresh Token 생성
        String accessToken = jwtUtil.generateAccessToken(userId, email, role);
        String refreshToken = jwtUtil.generateRefreshToken(userId, email);

        // 응답 DTO 반환
        return AuthDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public Member updateMember(String emailId, MemberDTO requestDTO) {
        Member member = getMemberByEmail(emailId);
        if (requestDTO.getNickname() != null && !requestDTO.getNickname().equals(member.getNickname())
                && memberRepository.existsByNickname(requestDTO.getNickname())) {
            throw new DuplicateNicknameException();
        }

        member.update(requestDTO);
        return memberRepository.save(member);
    }

    @Override
    public void updatePassword(String emailId, MemberDTO.PasswordRequestDTO requestDTO) {
        Member member = getMemberByEmail(emailId);
        if (!passwordEncoder.matches(requestDTO.getPassword(), member.getPassword())) {
            throw new PasswordIsInvalidException(); // 기존 비밀번호가 틀린 경우 예외 발생
        }

        if (requestDTO.getNewPassword() == null || requestDTO.getNewPassword().isBlank()) {
            throw new RequestException(ErrorCode.INVALID_REQUEST); // 새 비밀번호가 비어 있는 경우 예외 발생
        }

        // 기존 비밀번호와 새 비밀번호가 동일한지 체크
        if (passwordEncoder.matches(requestDTO.getNewPassword(), member.getPassword())) {
            throw new RequestException(ErrorCode.SAME_PASSWORD_NOT_ALLOWED); // 동일한 비밀번호로 변경 불가
        }

        String encodedNewPassword = passwordEncoder.encode(requestDTO.getNewPassword());
        member.updatePassword(encodedNewPassword);
        memberRepository.save(member);
    }

    @Override
    public void deleteMember(String emailId, MemberDTO.PasswordRequestDTO requestPassword) {
        Member member = getMemberByEmail(emailId);
        // 비밀번호 검증
        boolean passwordMatches = passwordEncoder.matches(requestPassword.getPassword(), member.getPassword());

        if (!passwordMatches) {
            throw new PasswordIsInvalidException();
        }
        memberRepository.delete(member);
        log.info("회원 삭제 - emailId: {}", emailId);
    }

    private Member getMemberByEmail(String emailId) {
        return memberRepository.findByEmail(emailId)
                .orElseThrow(MemberNotFoundException::new);
    }

    // 이메일 수동 검증 메서드 추가
    private void validateEmail(String email) {
        if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@mju\\.ac\\.kr$")) {
            throw new EmailIsInvalidException();
        }
    }
}