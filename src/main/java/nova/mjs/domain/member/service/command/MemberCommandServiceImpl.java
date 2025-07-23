package nova.mjs.domain.member.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.member.DTO.MemberDTO;
import nova.mjs.domain.member.entity.Member;
import nova.mjs.domain.member.exception.DuplicateNicknameException;
import nova.mjs.domain.member.exception.PasswordIsInvalidException;
import nova.mjs.domain.member.repository.MemberRepository;
import nova.mjs.domain.member.service.query.MemberQueryService;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.exception.request.RequestException;
import nova.mjs.util.jwt.JwtUtil;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3Service;
import nova.mjs.util.security.AuthDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {

    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;


    /**
     * S3에 프로필 이미지를 업로드하고 CloudFront URL 반환
     */
    @Override
    public String uploadProfileImage(MultipartFile file) {
        try {
            UUID folderUuid = UUID.randomUUID(); // 사용자 UUID 등으로 대체 가능
            return s3Service.uploadFile(file, S3DomainType.PROFILE_IMAGE, folderUuid);
        } catch (IOException e) {
            log.error("[프로필 이미지 업로드 실패]", e);
            throw new RequestException(ErrorCode.S3_IMAGE_UPLOAD_FAILED);
        }
    }


    /**
     * 회원 가입 로직
     */
    @Override
    public AuthDTO.LoginResponseDTO registerMember(MemberDTO.MemberRegistrationRequestDTO request) {
        // 회원이 입력한 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 닉네임 중복 확인
        memberQueryService.validateNicknameDuplication(request.getNickname());
        // 이메일 도메인 확인
        memberQueryService.validateEmailDomain(request.getEmail());
        // 이메일 중복 확인
        memberQueryService.validateEmailDuplication(request.getEmail());
        // 학번 중복 확인
        memberQueryService.validateStudentNumberDuplication(request.getStudentNumber());

        // 회원객체 생성
        Member newMember = Member.create(request, encodedPassword);
        newMember = memberRepository.save(newMember);

        UUID userId = newMember.getUuid();
        String email = newMember.getEmail();
        String role = String.valueOf(newMember.getRole());// Member 엔티티에 role 필드가 있어야 함

        // Access Token & Refresh Token 생성
        String accessToken = jwtUtil.generateAccessToken(userId, email, role);
        String refreshToken = jwtUtil.generateRefreshToken(userId, email);

        // 응답 DTO 반환
        return AuthDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 회원 정보 수정
    @Override
    public Member updateMember(String emailId, MemberDTO.MemberUpdateRequestDTO requestDTO) {
        Member member = memberQueryService.getMemberByEmail(emailId);
        if (requestDTO.getNickname() != null && !requestDTO.getNickname().equals(member.getNickname())
                && memberRepository.existsByNickname(requestDTO.getNickname())) {
            throw new DuplicateNicknameException();
        }
        if (requestDTO.getStudentNumber() != null && !requestDTO.getStudentNumber().equals(member.getStudentNumber())) {
            memberQueryService.validateStudentNumber(requestDTO.getStudentNumber());
        }
        member.update(requestDTO);
        return memberRepository.save(member);
    }

    // 비밀번호 변경
    @Transactional
    @Override
    public void updatePassword(String emailId, MemberDTO.PasswordRequestDTO requestDTO) {
        Member member = memberQueryService.getMemberByEmail(emailId);
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

    // 회원 삭제
    @Transactional
    @Override
    public void deleteMember(String emailId, MemberDTO.PasswordRequestDTO requestPassword) {
        Member member = memberQueryService.getMemberByEmail(emailId);
        // 비밀번호 검증
        boolean passwordMatches = passwordEncoder.matches(requestPassword.getPassword(), member.getPassword());

        if (!passwordMatches) {
            throw new PasswordIsInvalidException();
        }
        memberRepository.delete(member);
        log.info("회원 삭제 - emailId: {}", emailId);
    }
}
